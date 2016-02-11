package nl.tudelft.graphalytics;

import nl.tudelft.graphalytics.domain.NestedConfiguration;
import nl.tudelft.graphalytics.domain.system.SystemUnderTest;

/**
 * Partial implementation of the Platform interface that provides default no-op implementations for non-essential
 * methods.
 *
 * @author Tim Hegeman
 */
public abstract class AbstractPlatform implements Platform {

	@Override
	public NestedConfiguration getPlatformConfiguration() {
		return NestedConfiguration.empty();
	}

	@Override
	public SystemUnderTest getSystemUnderTest() {
		return SystemUnderTest.empty();
	}

}
