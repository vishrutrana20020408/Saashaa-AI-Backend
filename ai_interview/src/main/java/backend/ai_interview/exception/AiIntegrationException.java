package backend.ai_interview.exception;

/**
 * AiIntegrationException
 *
 * Custom exception for handling failures related to AI Engine integration.
 */
@SuppressWarnings("all")
public class AiIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Optional AI provider name
     */
    private String provider;

    /**
     * Optional operation type
     */
    private String operation;

    /**
     * Optional model name
     */
    private String model;

    public AiIntegrationException(String message) {
        super(message);
    }

    public AiIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AiIntegrationException(String message, String provider, String operation, String model) {
        super(message);
        this.provider = provider;
        this.operation = operation;
        this.model = model;
    }

    public AiIntegrationException(String message, Throwable cause, String provider, String operation, String model) {
        super(message, cause);
        this.provider = provider;
        this.operation = operation;
        this.model = model;
    }

    /**
     * Factory: AI request failed
     */
    public static AiIntegrationException requestFailed(String operation, Throwable cause) {
        return new AiIntegrationException(
                "AI request failed for operation: " + operation,
                cause,
                "AI_ENGINE",
                operation,
                null
        );
    }

    /**
     * Factory: invalid response
     */
    public static AiIntegrationException invalidResponse(String operation) {
        return new AiIntegrationException(
                "Invalid AI response received for operation: " + operation,
                "AI_ENGINE",
                operation,
                null
        );
    }

    /**
     * Factory: timeout
     */
    public static AiIntegrationException timeout(String operation) {
        return new AiIntegrationException(
                "AI request timeout for operation: " + operation,
                "AI_ENGINE",
                operation,
                null
        );
    }

    /**
     * Factory: model unavailable
     */
    public static AiIntegrationException modelUnavailable(String model) {
        return new AiIntegrationException(
                "AI model unavailable: " + model,
                "AI_ENGINE",
                "MODEL_ACCESS",
                model
        );
    }

    public String getProvider() {
        return provider;
    }

    public String getOperation() {
        return operation;
    }

    public String getModel() {
        return model;
    }
}