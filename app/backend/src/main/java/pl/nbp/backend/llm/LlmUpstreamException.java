package pl.nbp.backend.llm;

/**
 * Thrown when the upstream LLM service (OpenRouter) returns an error or
 * an unexpected response that prevents structured output from being parsed.
 *
 * <p>Maps to HTTP 502 (Bad Gateway) or 503 (Service Unavailable) at the
 * web layer — never fabricate a decision on LLM failure.
 */
public class LlmUpstreamException extends RuntimeException {

    /**
     * Creates an exception with a message and an underlying cause.
     *
     * @param message human-readable description of what went wrong
     * @param cause   the original exception from the HTTP client or JSON parser
     */
    public LlmUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with a message and no cause.
     *
     * @param message human-readable description of what went wrong
     */
    public LlmUpstreamException(String message) {
        super(message);
    }
}
