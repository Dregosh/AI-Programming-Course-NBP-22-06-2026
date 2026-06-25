package pl.nbp.backend.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory session that holds the full context of a single customer interaction.
 *
 * <p>A session is created when the customer submits the service request form and lives
 * in the {@code SessionStore} until it expires or is evicted. It accumulates the visual
 * assessment, the AI decision, and the full chat message history.
 *
 * <p>This class is intentionally <strong>mutable</strong>: messages are appended as the
 * conversation progresses. All access that crosses thread boundaries must be synchronized
 * externally (the {@code SessionStore} implementation is responsible).
 */
public class Session {

    /** Opaque session identifier, generated at creation time. */
    private final String sessionId;

    /** The original service request (without the raw image). */
    private final ServiceRequest request;

    /** The visual assessment produced by the multimodal step; may be {@code null} if not yet set. */
    private VisualAssessment assessment;

    /** The AI-produced decision; may be {@code null} if not yet set. */
    private Decision decision;

    /** Ordered list of all chat messages in this session. */
    private final List<ChatMessage> messages;

    /** UTC timestamp when the session was created. */
    private final Instant createdAt;

    /**
     * Creates a new session with an empty message history.
     *
     * @param sessionId opaque session identifier
     * @param request   the validated service request
     * @param createdAt the UTC creation timestamp
     */
    public Session(String sessionId, ServiceRequest request, Instant createdAt) {
        this.sessionId = sessionId;
        this.request = request;
        this.createdAt = createdAt;
        this.messages = new ArrayList<>();
    }

    /**
     * Returns the session identifier.
     *
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the service request associated with this session.
     *
     * @return the service request
     */
    public ServiceRequest getRequest() {
        return request;
    }

    /**
     * Returns the visual assessment, or {@code null} if not yet populated.
     *
     * @return the visual assessment, or {@code null}
     */
    public VisualAssessment getAssessment() {
        return assessment;
    }

    /**
     * Sets the visual assessment produced by the multimodal analysis step.
     *
     * @param assessment the visual assessment result
     */
    public void setAssessment(VisualAssessment assessment) {
        this.assessment = assessment;
    }

    /**
     * Returns the AI decision, or {@code null} if not yet populated.
     *
     * @return the decision, or {@code null}
     */
    public Decision getDecision() {
        return decision;
    }

    /**
     * Sets the decision produced by the decision agent.
     *
     * @param decision the structured decision
     */
    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    /**
     * Returns the ordered list of chat messages in this session.
     *
     * <p>The returned list is the live backing list; callers must not modify it directly.
     * Use {@link #addMessage(ChatMessage)} to append.
     *
     * @return the message list (unmodifiable view not enforced — use {@link #addMessage(ChatMessage)})
     */
    public List<ChatMessage> getMessages() {
        return messages;
    }

    /**
     * Appends a chat message to the session history.
     *
     * @param message the message to append
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    /**
     * Returns the UTC timestamp when this session was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
