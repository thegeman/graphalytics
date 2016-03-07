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

package nl.tudelft.graphalytics.configuration;

/**
 * Collection of properties as defined in Graphalytics's configuration files.
 *
 * @author Tim Hegeman
 */
public class GraphalyticsProperties {

	public static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";

	public static final String BENCHMARK_RUN_ALGORITHMS_KEY = "benchmark.run.algorithms";
	public static final String BENCHMARK_RUN_GRAPHS_KEY = "benchmark.run.graphs";
	public static final String BENCHMARK_RUN_OUTPUT_DIRECTORY_KEY = "benchmark.run.output-directory";
	public static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.run.output-required";
	public static final String BENCHMARK_RUN_TIME_LIMIT_EXECUTE_ALGORITHM_KEY = "benchmark.run.time-limit.execute-algorithm";
	public static final String BENCHMARK_RUN_TIME_LIMIT_UPLOAD_GRAPH_KEY = "benchmark.run.time-limit.upload-graph";

	public static final String GRAPHS_CACHE_DIRECTORY_KEY = "graphs.cache-directory";
	public static final String GRAPHS_NAMES_KEY = "graphs.names";
	public static final String GRAPHS_ROOT_DIRECTORY_KEY = "graphs.root-directory";

}
