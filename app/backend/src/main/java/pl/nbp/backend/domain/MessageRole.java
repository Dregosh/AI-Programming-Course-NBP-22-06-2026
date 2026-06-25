package pl.nbp.backend.domain;

/**
 * The role of a participant in the chat conversation.
 *
 * <p>Maps directly to the roles used by the OpenRouter Responses API.
 */
public enum MessageRole {

    /** System-level instruction injected at the start of the conversation context. */
    SYSTEM,

    /** Message authored by the end user (customer). */
    USER,

    /** Message authored by the AI assistant. */
    ASSISTANT
}
