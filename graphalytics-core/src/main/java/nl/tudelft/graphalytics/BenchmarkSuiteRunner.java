/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.BenchmarkResult.BenchmarkResultBuilder;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult.BenchmarkSuiteResultBuilder;
import nl.tudelft.graphalytics.execution.TimeLimitExceededException;
import nl.tudelft.graphalytics.plugin.Plugins;
import nl.tudelft.graphalytics.util.GraphFileManager;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static nl.tudelft.graphalytics.configuration.GraphalyticsProperties.BENCHMARK_PROPERTIES_FILE;
import static nl.tudelft.graphalytics.configuration.GraphalyticsProperties.BENCHMARK_RUN_TIME_LIMIT_EXECUTE_ALGORITHM_KEY;
import static nl.tudelft.graphalytics.configuration.GraphalyticsProperties.BENCHMARK_RUN_TIME_LIMIT_UPLOAD_GRAPH_KEY;

/**
 * Helper class for executing all benchmarks in a BenchmarkSuite on a specific Platform.
 *
 * @author Tim Hegeman
 */
public class BenchmarkSuiteRunner {
	private static final Logger LOG = LogManager.getLogger();

	private final BenchmarkSuite benchmarkSuite;
	private final Platform platform;
	private final Plugins plugins;

	private final ExecutorService executorService;
	private final long uploadGraphTimeout;
	private final long executeAlgorithmTimeout;

	/**
	 * @param benchmarkSuite the suite of benchmarks to run
	 * @param platform       the platform instance to run the benchmarks on
	 * @param plugins        collection of loaded plugins
	 */
	public BenchmarkSuiteRunner(BenchmarkSuite benchmarkSuite, Platform platform, Plugins plugins) throws ConfigurationException {
		this.benchmarkSuite = benchmarkSuite;
		this.platform = platform;
		this.plugins = plugins;

		Configuration graphConfiguration = new PropertiesConfiguration(BENCHMARK_PROPERTIES_FILE);
		executeAlgorithmTimeout = graphConfiguration.getInt(BENCHMARK_RUN_TIME_LIMIT_EXECUTE_ALGORITHM_KEY, -1);
		uploadGraphTimeout = graphConfiguration.getInt(BENCHMARK_RUN_TIME_LIMIT_UPLOAD_GRAPH_KEY, -1);

		executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
	}

	/**
	 * Executes the Graphalytics benchmark suite on the given platform. The benchmarks are grouped by graph so that each
	 * graph is uploaded to the platform exactly once. After executing all benchmarks for a specific graph, the graph
	 * is deleted from the platform.
	 *
	 * @return a BenchmarkSuiteResult object containing the gathered benchmark results and details
	 */
	public BenchmarkSuiteResult execute() {
		// TODO: Retrieve configuration for system, platform, and platform per benchmark

		// Use a BenchmarkSuiteResultBuilder to track the benchmark results gathered throughout execution
		BenchmarkSuiteResultBuilder benchmarkSuiteResultBuilder = new BenchmarkSuiteResultBuilder(benchmarkSuite);

		for (GraphSet graphSet : benchmarkSuite.getGraphSets()) {
			for (Graph graph : graphSet.getGraphs()) {
				// Skip the graph if there are no benchmarks to run on it
				if (benchmarkSuite.getBenchmarksForGraph(graph).isEmpty()) {
					continue;
				}

				// Ensure that the graph input files exist (i.e. generate them from the GraphSet sources if needed)
				try {
					GraphFileManager.ensureGraphFilesExist(graph);
				} catch (IOException ex) {
					LOG.error("Can not ensure that graph \"" + graph.getName() + "\" exists, skipping.", ex);
					continue;
				}

				// Upload the graph
				try {
					timeLimitUploadGraph(graph);
				} catch (Exception ex) {
					LOG.error("Failed to upload graph \"" + graph.getName() + "\", skipping.", ex);
					continue;
				}

				// Execute all benchmarks for this graph
				for (Benchmark benchmark : benchmarkSuite.getBenchmarksForGraph(graph)) {
					// Ensure that the output directory exists, if needed
					if (benchmark.isOutputRequired()) {
						try {
							Files.createDirectories(Paths.get(benchmark.getOutputPath()).getParent());
						} catch (IOException e) {
							LOG.error("Failed to create output directory \"" +
									Paths.get(benchmark.getOutputPath()).getParent() + "\", skipping.", e);
							continue;
						}
					}

					// Use a BenchmarkResultBuilder to create the BenchmarkResult for this Benchmark
					BenchmarkResultBuilder benchmarkResultBuilder = new BenchmarkResultBuilder(benchmark);

					LOG.info("Benchmarking algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graphSet.getName() + "\".");

					// Execute the pre-benchmark steps of all plugins
					plugins.preBenchmark(benchmark);

					// Start the timer
					benchmarkResultBuilder.markStartOfBenchmark();

					// Execute the benchmark and collect the result
					PlatformBenchmarkResult platformBenchmarkResult =
							new PlatformBenchmarkResult(NestedConfiguration.empty());
					boolean completedSuccessfully = false;
					try {
						platformBenchmarkResult = timeLimitExecuteAlgorithmOnGraph(benchmark);
						completedSuccessfully = true;
					} catch (PlatformExecutionException ex) {
						LOG.error("Algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
								graphSet.getName() + " failed to complete:", ex);
					}

					// Stop the timer
					benchmarkResultBuilder.markEndOfBenchmark(completedSuccessfully);

					LOG.info("Benchmarked algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graphSet.getName() + "\".");

					// Construct the BenchmarkResult and register it
					BenchmarkResult benchmarkResult = benchmarkResultBuilder.buildFromResult(platformBenchmarkResult);
					benchmarkSuiteResultBuilder.withBenchmarkResult(benchmarkResult);

					LOG.info("Benchmarking algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graphSet.getName() + "\" " + (completedSuccessfully ? "succeed" : "failed") + ".");
					long overallTime = (benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime());
					LOG.info("Benchmarking algorithm \"" + benchmark.getAlgorithm().getName() + "\" on graph \"" +
							graphSet.getName() + "\" took " + overallTime + " ms.");

					// Execute the post-benchmark steps of all plugins
					plugins.postBenchmark(benchmark, benchmarkResult);
				}

				// Delete the graph
				platform.deleteGraph(graph.getName());
			}
		}

		// Dump the used configuration
		NestedConfiguration benchmarkConfiguration = NestedConfiguration.empty();
		try {
			Configuration configuration = new PropertiesConfiguration("benchmark.properties");
			benchmarkConfiguration = NestedConfiguration.fromExternalConfiguration(configuration,
					"benchmark.properties");
		} catch (ConfigurationException e) {
			// Already reported during loading of benchmark
		}

		// Construct the BenchmarkSuiteResult
		return benchmarkSuiteResultBuilder.buildFromConfiguration(SystemDetails.empty(),
				benchmarkConfiguration,
				platform.getPlatformConfiguration());
	}

	private void timeLimitUploadGraph(final Graph graph) throws PlatformExecutionException {
		// Trigger the upload of a graph in a separate thread
		// A CountDownLatch is used to signal when the thread, and thus any computation, completes
		final CountDownLatch completionSignal = new CountDownLatch(1);
		Future<Boolean> uploadGraphFuture = executorService.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					platform.uploadGraph(graph);
					return true;
				} finally {
					completionSignal.countDown();
				}
			}
		});

		try {
			if (uploadGraphTimeout >= 0) {
				// If a time limit is specified for uploading a graph, wait at most that long for the graph to be uploaded
				try {
					uploadGraphFuture.get(uploadGraphTimeout, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					LOG.debug("Cancelling upload of graph {}", graph.getName());
					uploadGraphFuture.cancel(true);
					// Wait for the cancelled thread to complete to ensure that it does not interfere with future tasks
					completionSignal.await();
					throw new TimeLimitExceededException(String.format("Platform failed to upload graph %s within %d seconds", graph.getName(), uploadGraphTimeout));
				}
			} else {
				uploadGraphFuture.get();
			}
		} catch (InterruptedException e) {
			LOG.error("Aborting benchmark due to external interrupt.", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new PlatformExecutionException(String.format("Platform failed to upload graph %s", graph.getName()), e.getCause());
		}
	}

	private PlatformBenchmarkResult timeLimitExecuteAlgorithmOnGraph(final Benchmark benchmark) throws PlatformExecutionException {
		// Trigger the execution of an algorithm on a graph in a separate thread
		// A CountDownLatch is used to signal when the thread, and thus any computation, completes
		final CountDownLatch completionSignal = new CountDownLatch(1);
		Future<PlatformBenchmarkResult> executeAlgorithmFuture = executorService.submit(new Callable<PlatformBenchmarkResult>() {
			@Override
			public PlatformBenchmarkResult call() throws Exception {
				try {
					return platform.executeAlgorithmOnGraph(benchmark);
				} finally {
					completionSignal.countDown();
				}
			}
		});

		try {
			if (executeAlgorithmTimeout >= 0) {
				// If a time limit is specified for the execution of an algorithm, wait at most that long for the algorithm to complete
				try {
					return executeAlgorithmFuture.get(executeAlgorithmTimeout, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					LOG.debug("Cancelling execution of algorithm {} on graph {}", benchmark.getAlgorithm().getAcronym(), benchmark.getGraph().getName());
					executeAlgorithmFuture.cancel(true);
					// Wait for the cancelled thread to complete to ensure that it does not interfere with future tasks
					completionSignal.await();
					throw new TimeLimitExceededException(String.format("Platform failed to execute algorithm %s on graph %s within %d seconds",
							benchmark.getAlgorithm().getAcronym(), benchmark.getGraph().getName(), executeAlgorithmTimeout));
				}
			} else {
				return executeAlgorithmFuture.get();
			}
		} catch (InterruptedException e) {
			LOG.error("Aborting benchmark due to external interrupt.", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new PlatformExecutionException(String.format("Platform failed to execute algorithm %s on graph %s",
					benchmark.getAlgorithm().getAcronym(), benchmark.getGraph().getName()), e.getCause());
		}
	}

}
