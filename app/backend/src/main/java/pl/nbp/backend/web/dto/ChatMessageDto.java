package pl.nbp.backend.web.dto;

import pl.nbp.backend.domain.MessageRole;

import java.time.Instant;

/**
 * Response DTO representing a single chat message.
 *
 * @param role      the role of the message author (USER / ASSISTANT / SYSTEM)
 * @param content   the message text content
 * @param createdAt the UTC timestamp when the message was created
 */
public record ChatMessageDto(
        MessageRole role,
        String content,
        Instant createdAt) {
}
