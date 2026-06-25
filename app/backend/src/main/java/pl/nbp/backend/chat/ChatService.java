package pl.nbp.backend.chat;

import org.springframework.stereotype.Service;
import pl.nbp.backend.domain.ChatMessage;
import pl.nbp.backend.domain.MessageRole;
import pl.nbp.backend.domain.Session;
import pl.nbp.backend.llm.LlmClient;
import pl.nbp.backend.session.SessionStore;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * Service responsible for handling chat interactions within an existing session.
 *
 * <p>Appends each user message to the session history, streams the assistant reply
 * via {@link LlmClient#streamChat}, then persists the updated session.
 */
@Service
public class ChatService {

    private final SessionStore sessionStore;
    private final LlmClient llmClient;

    /**
     * Creates a new {@code ChatService}.
     *
     * @param sessionStore store for loading and persisting sessions
     * @param llmClient    LLM port for streaming chat completions
     */
    public ChatService(SessionStore sessionStore, LlmClient llmClient) {
        this.sessionStore = sessionStore;
        this.llmClient = llmClient;
    }

    /**
     * Appends the user message to the session history, streams the assistant reply,
     * then appends the assistant reply and persists the session.
     *
     * @param sessionId session identifier
     * @param userText  user's message text
     * @param onDelta   consumer called for each streamed token
     * @return the full assistant reply text
     * @throws SessionNotFoundException if no session with the given id exists
     */
    public String sendMessage(String sessionId, String userText, Consumer<String> onDelta) {
        Session session = sessionStore.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Append user message
        ChatMessage userMsg = new ChatMessage(MessageRole.USER, userText, Instant.now());
        session.addMessage(userMsg);

        // Stream assistant reply
        String reply = llmClient.streamChat(session.getMessages(), onDelta);

        // Append assistant reply
        ChatMessage assistantMsg = new ChatMessage(MessageRole.ASSISTANT, reply, Instant.now());
        session.addMessage(assistantMsg);

        // Persist updated session
        sessionStore.save(session);

        return reply;
    }
}
