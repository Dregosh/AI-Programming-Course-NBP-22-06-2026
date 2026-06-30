package pl.nbp.backend.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.nbp.backend.chat.ChatService;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link MessageController}.
 *
 * <p>Verifies that the SSE contract matches ADR-001 §4-5:
 * <ul>
 *   <li>JSON body {@code {"content":"..."}} is accepted</li>
 *   <li>SSE events are named {@code delta} / {@code done} / {@code error}</li>
 *   <li>{@code delta} data is JSON {@code {"token":"..."}}</li>
 *   <li>{@code done} data is JSON {@code {"finishReason":"stop"}}</li>
 *   <li>On upstream failure an {@code error} event is sent</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("POST /api/sessions/{id}/messages — SSE contract")
class MessageControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChatService chatService;

    // -----------------------------------------------------------------------
    // Test 1 – happy path: delta + done events
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("accepts JSON body and streams named delta + done events")
    void streamMessage_acceptsJsonBody_streamsDeltaAndDoneEvents() throws Exception {
        // Arrange: ChatService emits one token "Hello" then returns the full reply
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<String> onDelta = invocation.getArgument(2);
            onDelta.accept("Hello");
            return "Hello";
        }).when(chatService).sendMessage(eq("test-session"), eq("hello"), any());

        // Act: start async SSE request
        MvcResult asyncResult = mockMvc.perform(
                        post("/api/sessions/test-session/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Dispatch async response and collect the body
        String body = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert: contains named delta event
        assertThat(body).contains("event:delta");

        // Assert: delta data parses to {"token":"Hello"}
        String deltaData = extractEventData(body, "delta");
        assertThat(deltaData).isNotNull();
        JsonNode deltaJson = objectMapper.readTree(deltaData);
        assertThat(deltaJson.get("token").asText()).isEqualTo("Hello");

        // Assert: contains named done event
        assertThat(body).contains("event:done");

        // Assert: done data contains finishReason: stop
        String doneData = extractEventData(body, "done");
        assertThat(doneData).isNotNull();
        JsonNode doneJson = objectMapper.readTree(doneData);
        assertThat(doneJson.get("finishReason").asText()).isEqualTo("stop");
    }

    // -----------------------------------------------------------------------
    // Test 2 – error path: error event
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sends named error event when ChatService throws")
    void streamMessage_onError_sendsErrorEvent() throws Exception {
        // Arrange: ChatService throws a runtime exception
        doThrow(new RuntimeException("upstream failure"))
                .when(chatService).sendMessage(eq("test-session"), any(), any());

        // Act: start async SSE request
        MvcResult asyncResult = mockMvc.perform(
                        post("/api/sessions/test-session/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Dispatch async response and collect the body
        String body = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert: contains named error event
        assertThat(body).contains("event:error");

        // Assert: error data has code and message fields
        String errorData = extractEventData(body, "error");
        assertThat(errorData).isNotNull();
        JsonNode errorJson = objectMapper.readTree(errorData);
        assertThat(errorJson.has("code")).isTrue();
        assertThat(errorJson.has("message")).isTrue();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Extracts the data payload of the first SSE event with the given name.
     *
     * <p>SSE wire format (may have or omit spaces after colon):
     * <pre>
     * event:delta\n
     * data:{"token":"Hello"}\n
     * \n
     * </pre>
     *
     * @param body      the full SSE response body
     * @param eventName the event name to look for
     * @return the data string, or {@code null} if not found
     */
    private String extractEventData(String body, String eventName) {
        String[] lines = body.split("\n");
        boolean nextIsData = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equals("event:" + eventName) || trimmed.equals("event: " + eventName)) {
                nextIsData = true;
                continue;
            }
            if (nextIsData && (trimmed.startsWith("data:") || trimmed.startsWith("data: "))) {
                return trimmed.replaceFirst("data:\\s*", "");
            }
            if (nextIsData && !trimmed.isEmpty()) {
                // unexpected line between event and data — reset
                nextIsData = false;
            }
        }
        return null;
    }
}
