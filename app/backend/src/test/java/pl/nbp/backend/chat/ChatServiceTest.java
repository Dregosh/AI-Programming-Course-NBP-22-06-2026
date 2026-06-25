package pl.nbp.backend.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.nbp.backend.domain.*;
import pl.nbp.backend.llm.LlmClient;
import pl.nbp.backend.session.SessionStore;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock SessionStore sessionStore;
    @Mock LlmClient llmClient;

    @InjectMocks ChatService chatService;

    private Session session;

    @BeforeEach
    void setUp() {
        ServiceRequest req = new ServiceRequest(
                RequestType.RETURN, EquipmentCategory.SMARTPHONE,
                "iPhone 14", LocalDate.now().minusDays(10), null);
        session = new Session("sess-1", req, Instant.now());
    }

    @Test
    void sendMessage_happyPath_returnsReply() {
        when(sessionStore.findById("sess-1")).thenReturn(Optional.of(session));
        when(llmClient.streamChat(anyList(), any(Consumer.class))).thenReturn("Odpowiedź asystenta");

        String reply = chatService.sendMessage("sess-1", "Pytanie klienta", token -> {});

        assertThat(reply).isEqualTo("Odpowiedź asystenta");
        assertThat(session.getMessages()).hasSize(2); // user + assistant
        assertThat(session.getMessages().get(0).role()).isEqualTo(MessageRole.USER);
        assertThat(session.getMessages().get(1).role()).isEqualTo(MessageRole.ASSISTANT);
        verify(sessionStore).save(session);
    }

    @Test
    void sendMessage_unknownSession_throwsSessionNotFoundException() {
        when(sessionStore.findById("no-such")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage("no-such", "text", token -> {}))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("no-such");
    }

    @Test
    void sendMessage_streamingTokensAreForwarded() {
        when(sessionStore.findById("sess-1")).thenReturn(Optional.of(session));
        // Simulate streaming: call onDelta with tokens
        doAnswer(inv -> {
            Consumer<String> consumer = inv.getArgument(1);
            consumer.accept("token1");
            consumer.accept("token2");
            return "token1token2";
        }).when(llmClient).streamChat(anyList(), any(Consumer.class));

        StringBuilder collected = new StringBuilder();
        chatService.sendMessage("sess-1", "Pytanie", collected::append);

        assertThat(collected.toString()).isEqualTo("token1token2");
    }
}
