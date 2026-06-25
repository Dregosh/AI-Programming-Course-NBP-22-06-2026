package pl.nbp.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pl.nbp.backend.decision.CreateSessionResult;
import pl.nbp.backend.decision.DecisionOrchestrator;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.DecisionOutcome;
import pl.nbp.backend.domain.EquipmentCategory;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.ServiceRequest;
import pl.nbp.backend.domain.Session;
import pl.nbp.backend.session.SessionStore;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DecisionOrchestrator orchestrator;

    @MockBean
    SessionStore sessionStore;

    private byte[] makeJpegBytes() throws Exception {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", baos);
        return baos.toByteArray();
    }

    @Test
    void createSession_returns201() throws Exception {
        byte[] jpegBytes = makeJpegBytes();
        Decision decision = new Decision(DecisionOutcome.APPROVE, false, "OK", List.of(), List.of());
        CreateSessionResult result = new CreateSessionResult(UUID.randomUUID().toString(), decision, "Zaakceptowano", Instant.now());

        when(orchestrator.createSession(any(ServiceRequest.class), any(), any())).thenReturn(result);

        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", jpegBytes);

        mockMvc.perform(multipart("/api/sessions")
                        .file(image)
                        .param("requestType", "RETURN")
                        .param("category", "SMARTPHONE")
                        .param("modelName", "iPhone 14")
                        .param("purchaseDate", "2024-01-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.decision.outcome").value("APPROVE"));
    }

    @Test
    void getSession_existingSession_returns200() throws Exception {
        ServiceRequest req = new ServiceRequest(
                RequestType.RETURN, EquipmentCategory.SMARTPHONE,
                "iPhone 14", LocalDate.now().minusDays(10), null);
        Session session = new Session("sess-123", req, Instant.now());
        Decision decision = new Decision(DecisionOutcome.APPROVE, false, "OK", List.of(), List.of());
        session.setDecision(decision);

        when(sessionStore.findById("sess-123")).thenReturn(Optional.of(session));

        mockMvc.perform(get("/api/sessions/sess-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-123"))
                .andExpect(jsonPath("$.decision.outcome").value("APPROVE"));
    }

    @Test
    void getSession_unknownSession_returns404() throws Exception {
        when(sessionStore.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sessions/unknown"))
                .andExpect(status().isNotFound());
    }
}
