package pl.nbp.backend.decision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.nbp.backend.domain.*;
import pl.nbp.backend.image.ImageService;
import pl.nbp.backend.llm.LlmClient;
import pl.nbp.backend.policy.PolicyProvider;
import pl.nbp.backend.session.SessionStore;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionOrchestratorTest {

    @Mock LlmClient llmClient;
    @Mock PolicyProvider policyProvider;
    @Mock ImageService imageService;
    @Mock SessionStore sessionStore;

    @InjectMocks DecisionOrchestrator orchestrator;

    private ServiceRequest returnRequest;
    private VisualAssessment goodAssessment;
    private Decision approveDecision;

    @BeforeEach
    void setUp() {
        returnRequest = new ServiceRequest(
                RequestType.RETURN, EquipmentCategory.SMARTPHONE,
                "iPhone 14", LocalDate.now().minusDays(10), null);

        goodAssessment = VisualAssessment.builder()
                .analyzable(true).confidence(0.9).notes("Good")
                .signsOfUse(false).damageObserved(false).resalableAsNew(true)
                .build();

        approveDecision = new Decision(
                DecisionOutcome.APPROVE, false,
                "Sprzęt w dobrym stanie.", List.of("Wyślij sprzęt"), List.of("§1"));
    }

    @Test
    void createSession_happyPath_returnsResult() {
        when(imageService.validateAndCompress(any(), any())).thenReturn("data:image/jpeg;base64,abc");
        when(llmClient.analyzeImage(any(), any(), any())).thenReturn(goodAssessment);
        when(policyProvider.getPolicyText(any())).thenReturn("polityka zwrotów");
        when(llmClient.decide(any(), any(), any(), any())).thenReturn(approveDecision);

        CreateSessionResult result = orchestrator.createSession(returnRequest, new byte[]{1, 2, 3}, "image/jpeg");

        assertThat(result.sessionId()).isNotBlank();
        assertThat(result.decision().outcome()).isEqualTo(DecisionOutcome.APPROVE);
        assertThat(result.decision().binding()).isTrue();
        assertThat(result.firstMessage()).contains("zaakceptowany");
        assertThat(result.createdAt()).isNotNull();
        verify(sessionStore).save(any(Session.class));
    }

    @Test
    void createSession_sessionIsSavedWithAssessmentAndDecision() {
        when(imageService.validateAndCompress(any(), any())).thenReturn("data:image/jpeg;base64,abc");
        when(llmClient.analyzeImage(any(), any(), any())).thenReturn(goodAssessment);
        when(policyProvider.getPolicyText(any())).thenReturn("polityka");
        when(llmClient.decide(any(), any(), any(), any())).thenReturn(approveDecision);

        orchestrator.createSession(returnRequest, new byte[]{1, 2, 3}, "image/jpeg");

        verify(sessionStore).save(argThat(session ->
                session.getAssessment() != null &&
                session.getDecision() != null &&
                !session.getMessages().isEmpty()
        ));
    }

    @Test
    void buildFormContext_formatsAllFields() {
        String context = orchestrator.buildFormContext(returnRequest);
        assertThat(context).contains("RETURN");
        assertThat(context).contains("SMARTPHONE");
        assertThat(context).contains("iPhone 14");
    }

    @Test
    void composeFirstMessage_rejectContainsCriteria() {
        Decision rejectDecision = new Decision(
                DecisionOutcome.REJECT, false,
                "Brak podstaw do zwrotu.", List.of(), List.of());
        String msg = orchestrator.composeFirstMessage(rejectDecision);
        assertThat(msg).contains("kryteriów");
    }
}
