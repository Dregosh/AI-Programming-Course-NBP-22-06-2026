package pl.nbp.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.nbp.backend.chat.ChatService;
import pl.nbp.backend.web.dto.PostMessageRequest;

import java.io.IOException;
import java.util.Map;

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
     * <p>Each SSE event is named and carries JSON data:
     * <ul>
     *   <li>{@code delta} — {@code {"token":"..."}} for every streamed token</li>
     *   <li>{@code done}  — {@code {"finishReason":"stop"}} once the reply is complete</li>
     *   <li>{@code error} — {@code {"code":"...","message":"..."}} on upstream failure</li>
     * </ul>
     *
     * @param sessionId the session identifier
     * @param body      the request body containing the user message text
     * @return an SSE emitter that streams the assistant reply
     */
    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @PathVariable String sessionId,
            @RequestBody PostMessageRequest body) {

        SseEmitter emitter = new SseEmitter(60_000L);

        taskExecutor.execute(() -> {
            try {
                chatService.sendMessage(sessionId, body.content(), token -> {
                    try {
                        String json = objectMapper.writeValueAsString(Map.of("token", token));
                        emitter.send(SseEmitter.event().name("delta").data(json, MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                String doneJson = objectMapper.writeValueAsString(Map.of("finishReason", "stop"));
                emitter.send(SseEmitter.event().name("done").data(doneJson, MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception e) {
                try {
                    String errorJson = objectMapper.writeValueAsString(
                            Map.of("code", "UPSTREAM_ERROR", "message",
                                    e.getMessage() != null ? e.getMessage() : "unknown error"));
                    emitter.send(SseEmitter.event().name("error").data(errorJson, MediaType.APPLICATION_JSON));
                    emitter.complete();
                } catch (IOException ioEx) {
                    emitter.completeWithError(ioEx);
                }
            }
        });

        return emitter;
    }
}
