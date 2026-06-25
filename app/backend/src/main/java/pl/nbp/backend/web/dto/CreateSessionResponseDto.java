package pl.nbp.backend.web.dto;

import java.time.Instant;

/**
 * Response DTO returned by {@code POST /api/sessions}.
 *
 * @param sessionId    the newly created session's opaque identifier
 * @param decision     the structured AI decision
 * @param firstMessage the pre-composed first assistant message in Polish
 * @param createdAt    the UTC timestamp when the session was created
 */
public record CreateSessionResponseDto(
        String sessionId,
        DecisionDto decision,
        String firstMessage,
        Instant createdAt) {
}
