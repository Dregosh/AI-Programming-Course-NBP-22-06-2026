package pl.nbp.backend.chat;

/**
 * Thrown when a session with the given identifier cannot be found in the store.
 *
 * <p>Maps to HTTP 404 when caught by the global exception handler.
 */
public class SessionNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for the given session identifier.
     *
     * @param sessionId the identifier of the session that was not found
     */
    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
    }
}
