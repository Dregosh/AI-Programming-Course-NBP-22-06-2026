package pl.nbp.backend.web;

/**
 * Thrown when a complaint request is submitted without the required {@code reason} field.
 *
 * <p>Maps to HTTP 400 Bad Request when caught by the global exception handler.
 */
public class ComplaintReasonRequiredException extends RuntimeException {

    /** Creates a new exception with a Polish error message. */
    public ComplaintReasonRequiredException() {
        super("Pole 'reason' jest wymagane dla wniosku reklamacyjnego.");
    }
}
