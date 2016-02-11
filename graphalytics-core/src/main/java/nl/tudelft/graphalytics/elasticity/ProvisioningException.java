package nl.tudelft.graphalytics.elasticity;

/**
 * Base class for exceptions that occurs while processing a ProvisioningRequest in an ElasticPlatform.
 *
 * @author Tim Hegeman
 */
public class ProvisioningException extends Exception {

	public ProvisioningException(String message) {
		super(message);
	}

	public ProvisioningException(String message, Throwable cause) {
		super(message, cause);
	}

}
