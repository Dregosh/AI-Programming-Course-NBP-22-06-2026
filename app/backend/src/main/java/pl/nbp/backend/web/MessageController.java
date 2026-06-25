package pl.nbp.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.nbp.backend.chat.ChatService;

import java.io.IOException;

/**
 * REST controller for streaming chat messages over Server-Sent Events.
 *
 * <p>Exposes:
 * <ul>
 *   <li>{@code POST /api/sessions/{sessionId}/messages} — stream an assistant reply as SSE</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class MessageController {

    private final ChatService chatService;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new {@code MessageController}.
     *
     * @param chatService  the chat service
     * @param taskExecutor the SSE task executor
     * @param objectMapper the Jackson object mapper
     */
    public MessageController(ChatService chatService,
                             @Qualifier("sseTaskExecutor") TaskExecutor taskExecutor,
                             ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a user message and streams the assistant reply as Server-Sent Events.
     *
     * <p>Each SSE event carries one text token from the LLM stream. The emitter
     * completes normally when the response is finished, or with an error on failure.
     *
     * @param sessionId the session identifier
     * @param message   the user message text
     * @return an SSE emitter that streams the assistant reply
     */
    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @PathVariable String sessionId,
            @RequestParam("message") String message) {

        SseEmitter emitter = new SseEmitter(60_000L);

        taskExecutor.execute(() -> {
            try {
                chatService.sendMessage(sessionId, message, token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
