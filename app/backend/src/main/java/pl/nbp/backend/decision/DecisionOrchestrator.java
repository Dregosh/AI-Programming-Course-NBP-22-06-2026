package pl.nbp.backend.decision;

import org.springframework.stereotype.Service;
import pl.nbp.backend.domain.ChatMessage;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.DecisionOutcome;
import pl.nbp.backend.domain.MessageRole;
import pl.nbp.backend.domain.ServiceRequest;
import pl.nbp.backend.domain.Session;
import pl.nbp.backend.domain.VisualAssessment;
import pl.nbp.backend.image.ImageService;
import pl.nbp.backend.llm.LlmClient;
import pl.nbp.backend.policy.PolicyProvider;
import pl.nbp.backend.session.SessionStore;

import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrates the full create-session pipeline: image validation and compression,
 * visual analysis, policy-based decision, first message composition, and session
 * persistence.
 *
 * <p>This service is the single entry point called by the web layer when the customer
 * submits the service request form. It is intentionally free of any HTTP concerns.
 *
 * <p>Dependency direction: {@code decision} → {@code image}, {@code llm}, {@code policy},
 * {@code session} (never back towards {@code web}).
 */
@Service
public class DecisionOrchestrator {

    private final LlmClient llmClient;
    private final PolicyProvider policyProvider;
    private final ImageService imageService;
    private final SessionStore sessionStore;

    /**
     * Creates a new {@code DecisionOrchestrator}.
     *
     * @param llmClient      the LLM port for image analysis and decision making
     * @param policyProvider loads the applicable policy text for each request type
     * @param imageService   validates and compresses the uploaded image
     * @param sessionStore   persists the created session
     */
    public DecisionOrchestrator(LlmClient llmClient, PolicyProvider policyProvider,
                                ImageService imageService, SessionStore sessionStore) {
        this.llmClient = llmClient;
        this.policyProvider = policyProvider;
        this.imageService = imageService;
        this.sessionStore = sessionStore;
    }

    /**
     * Creates a new session: validates and compresses the image, runs visual analysis,
     * applies policy to make a decision, stores the session, and returns the result.
     *
     * <p>The pipeline steps are:
     * <ol>
     *   <li>Validate and compress the image via {@link ImageService}.</li>
     *   <li>Build a compact form context string.</li>
     *   <li>Analyse the image via {@link LlmClient#analyzeImage}.</li>
     *   <li>Retrieve the applicable policy text.</li>
     *   <li>Produce a decision via {@link LlmClient#decide}.</li>
     *   <li>Create and persist the {@link Session}.</li>
     *   <li>Compose and record the first assistant message.</li>
     * </ol>
     *
     * @param request      the validated service request from the form
     * @param imageBytes   raw bytes of the uploaded image
     * @param contentType  the declared MIME type of the image (used by {@link ImageService})
     * @return the session result including sessionId, decision, first message, and timestamp
     */
    public CreateSessionResult createSession(ServiceRequest request, byte[] imageBytes, String contentType) {
        // 1. Validate and compress image
        String imageDataUrl = imageService.validateAndCompress(imageBytes, contentType);

        // 2. Build form context
        String formContext = buildFormContext(request);

        // 3. Analyze image
        VisualAssessment assessment = llmClient.analyzeImage(request.requestType(), imageDataUrl, formContext);

        // 4. Get policy text
        String policyText = policyProvider.getPolicyText(request.requestType());

        // 5. Make decision
        Decision decision = llmClient.decide(request.requestType(), assessment, formContext, policyText);

        // 6. Create and persist session
        String sessionId = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        Session session = new Session(sessionId, request, createdAt);
        session.setAssessment(assessment);
        session.setDecision(decision);

        // 7. Compose first assistant message and add to history
        String firstMessage = composeFirstMessage(decision);
        session.addMessage(new ChatMessage(MessageRole.ASSISTANT, firstMessage, Instant.now()));

        // 8. Save session
        sessionStore.save(session);

        return new CreateSessionResult(sessionId, decision, firstMessage, createdAt);
    }

    /**
     * Builds a compact plain-text summary of the service request form fields,
     * suitable for use as LLM context.
     *
     * @param request the service request to summarise
     * @return multi-line string with labelled field values in Polish
     */
    String buildFormContext(ServiceRequest request) {
        return "Typ wniosku: " + request.requestType() +
               "\nKategoria: " + request.category() +
               "\nModel: " + request.modelName() +
               "\nData zakupu: " + request.purchaseDate() +
               "\nOpis: " + request.reason();
    }

    /**
     * Composes the first assistant message shown to the customer based on the decision outcome.
     *
     * <p>The message is always in Polish and begins with a brief outcome summary followed
     * by the LLM-produced justification.
     *
     * @param decision the structured decision
     * @return the first assistant message in Polish
     */
    String composeFirstMessage(Decision decision) {
        return switch (decision.outcome()) {
            case APPROVE -> "Twój wniosek został wstępnie zaakceptowany. " + decision.justification();
            case REJECT -> "Niestety, Twój wniosek nie spełnia kryteriów. " + decision.justification();
            case ESCALATE -> "Twój wniosek wymaga dodatkowej weryfikacji przez specjalistę. " + decision.justification();
        };
    }
}
