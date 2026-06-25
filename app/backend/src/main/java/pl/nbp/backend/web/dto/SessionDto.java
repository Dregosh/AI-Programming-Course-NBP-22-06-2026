package pl.nbp.backend.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO returned by {@code GET /api/sessions/{sessionId}}.
 *
 * @param sessionId the session's opaque identifier
 * @param decision  the AI decision (may be {@code null} if not yet produced)
 * @param messages  ordered list of chat messages in this session
 * @param createdAt the UTC timestamp when the session was created
 */
public record SessionDto(
        String sessionId,
        DecisionDto decision,
        List<ChatMessageDto> messages,
        Instant createdAt) {
}
