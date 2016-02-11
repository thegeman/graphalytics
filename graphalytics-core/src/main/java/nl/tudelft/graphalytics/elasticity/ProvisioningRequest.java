package nl.tudelft.graphalytics.elasticity;

/**
 * Request for an ElasticPlatform to provision a set of resources.
 *
 * @author Tim Hegeman
 */
public class ProvisioningRequest {

	private final int numberOfNodes;

	public ProvisioningRequest(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

}
