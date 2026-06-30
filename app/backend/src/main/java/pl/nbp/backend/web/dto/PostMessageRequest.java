package pl.nbp.backend.web.dto;

/**
 * Request body for {@code POST /api/sessions/{sessionId}/messages}.
 *
 * @param content the user message text
 */
public record PostMessageRequest(String content) {}
