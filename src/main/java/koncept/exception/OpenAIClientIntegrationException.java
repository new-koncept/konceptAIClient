package koncept.exception;

public class OpenAIClientIntegrationException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public OpenAIClientIntegrationException(final String message, final int statusCode, final String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
