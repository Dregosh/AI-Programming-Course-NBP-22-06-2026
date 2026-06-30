package pl.nbp.backend.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.nbp.backend.chat.SessionNotFoundException;
import pl.nbp.backend.image.ImageTooLargeException;
import pl.nbp.backend.image.UnsupportedImageTypeException;
import pl.nbp.backend.llm.LlmUpstreamException;
import pl.nbp.backend.web.dto.ApiErrorDto;

/**
 * Global exception handler that maps domain exceptions to structured HTTP error responses.
 *
 * <p>All user-facing messages are in Polish.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link SessionNotFoundException} → HTTP 404.
     *
     * @param ex the exception
     * @return 404 response with error details
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleSessionNotFound(SessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto(404, ex.getMessage(), null));
    }

    /**
     * Handles {@link ComplaintReasonRequiredException} → HTTP 400.
     *
     * @param ex the exception
     * @return 400 response with error details
     */
    @ExceptionHandler(ComplaintReasonRequiredException.class)
    public ResponseEntity<ApiErrorDto> handleComplaintReasonRequired(ComplaintReasonRequiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(400, ex.getMessage(), null));
    }

    /**
     * Handles {@link ImageTooLargeException} → HTTP 413.
     *
     * @param ex the exception
     * @return 413 response with error details
     */
    @ExceptionHandler(ImageTooLargeException.class)
    public ResponseEntity<ApiErrorDto> handleImageTooLarge(ImageTooLargeException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorDto(413, ex.getMessage(), null));
    }

    /**
     * Handles {@link UnsupportedImageTypeException} → HTTP 415.
     *
     * @param ex the exception
     * @return 415 response with error details
     */
    @ExceptionHandler(UnsupportedImageTypeException.class)
    public ResponseEntity<ApiErrorDto> handleUnsupportedImageType(UnsupportedImageTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiErrorDto(415, ex.getMessage(), null));
    }

    /**
     * Handles {@link LlmUpstreamException} → HTTP 502.
     *
     * @param ex the exception
     * @return 502 response with a generic Polish error message
     */
    @ExceptionHandler(LlmUpstreamException.class)
    public ResponseEntity<ApiErrorDto> handleLlmUpstream(LlmUpstreamException ex) {
        log.error("LLM upstream error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorDto(502, "Usługa AI niedostępna. Spróbuj ponownie.", null));
    }

    /**
     * Handles {@link IllegalArgumentException} → HTTP 400.
     *
     * @param ex the exception
     * @return 400 response with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(400, ex.getMessage(), null));
    }

    /**
     * Fallback handler for all unhandled exceptions → HTTP 500.
     *
     * @param ex the exception
     * @return 500 response with a generic Polish error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDto(500, "Błąd wewnętrzny serwera.", null));
    }
}
