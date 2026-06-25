package pl.nbp.backend.web.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for API error responses.
 *
 * @param status  the HTTP status code
 * @param message the human-readable error message in Polish
 * @param fields  optional map of field-level validation errors; {@code null} when not applicable
 */
public record ApiErrorDto(
        int status,
        String message,
        Map<String, List<String>> fields) {
}
