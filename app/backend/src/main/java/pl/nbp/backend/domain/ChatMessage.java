package pl.nbp.backend.domain;

import java.time.Instant;

/**
 * A single message in the chat conversation associated with a session.
 *
 * <p>Messages are immutable and ordered by {@link #createdAt()} within a session.
 *
 * @param role      the role of the message author (SYSTEM, USER, or ASSISTANT)
 * @param content   the message content in plain text or Markdown
 * @param createdAt the UTC timestamp when the message was created
 */
public record ChatMessage(
        MessageRole role,
        String content,
        Instant createdAt) {
}
