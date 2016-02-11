package nl.tudelft.graphalytics.elasticity;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.domain.system.SystemUnderTest;

/**
 * Extension of Platform interface with support for elasticity, i.e. provisioning different numbers and/or types of
 * resources for the system under test between benchmarks.
 *
 * @author Tim Hegeman
 */
public interface ElasticPlatform extends Platform {

	/**
	 * Provisions a given amount of resources for the system under test, as specified by a ProvisioningRequest. The
	 * ProvisioningRequest indicates the total amount of resources that should be available after provisioning, i.e.
	 * it does not represent an incremental change from any previously provisioned resources.
	 *
	 * @param request the requested amount of resources to be provisioned
	 * @return a description of the system under test after provisioning
	 */
	SystemUnderTest provision(ProvisioningRequest request) throws ProvisioningException;

}
