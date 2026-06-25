package pl.nbp.backend.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.nbp.backend.chat.SessionNotFoundException;
import pl.nbp.backend.decision.CreateSessionResult;
import pl.nbp.backend.decision.DecisionOrchestrator;
import pl.nbp.backend.domain.EquipmentCategory;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.ServiceRequest;
import pl.nbp.backend.domain.Session;
import pl.nbp.backend.session.SessionStore;
import pl.nbp.backend.web.dto.CreateSessionResponseDto;
import pl.nbp.backend.web.dto.SessionDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * REST controller for session lifecycle operations.
 *
 * <p>Exposes:
 * <ul>
 *   <li>{@code POST /api/sessions} — create a new decision session</li>
 *   <li>{@code GET /api/sessions/{sessionId}} — retrieve an existing session</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class SessionController {

    private final DecisionOrchestrator orchestrator;
    private final SessionStore sessionStore;

    /**
     * Creates a new {@code SessionController}.
     *
     * @param orchestrator the decision orchestrator service
     * @param sessionStore the session store
     */
    public SessionController(DecisionOrchestrator orchestrator, SessionStore sessionStore) {
        this.orchestrator = orchestrator;
        this.sessionStore = sessionStore;
    }

    /**
     * Creates a new decision session from a multipart form submission.
     *
     * @param requestType  the type of service request (COMPLAINT or RETURN)
     * @param category     the equipment category
     * @param modelName    the equipment model name
     * @param purchaseDate the purchase date (optional)
     * @param reason       the complaint reason (required when requestType is COMPLAINT)
     * @param image        the equipment image
     * @return HTTP 201 with the session and decision details
     * @throws IOException                       if image bytes cannot be read
     * @throws ComplaintReasonRequiredException  if requestType is COMPLAINT and reason is blank
     */
    @PostMapping(value = "/sessions", consumes = "multipart/form-data")
    public ResponseEntity<CreateSessionResponseDto> createSession(
            @RequestParam("requestType") RequestType requestType,
            @RequestParam("category") EquipmentCategory category,
            @RequestParam("modelName") String modelName,
            @RequestParam(value = "purchaseDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam("image") MultipartFile image) throws IOException {

        if (requestType == RequestType.COMPLAINT && (reason == null || reason.isBlank())) {
            throw new ComplaintReasonRequiredException();
        }

        ServiceRequest request = new ServiceRequest(requestType, category, modelName, purchaseDate, reason);
        CreateSessionResult result = orchestrator.createSession(request, image.getBytes(), image.getContentType());
        return ResponseEntity.status(201).body(DtoMapper.toCreateSessionResponse(result));
    }

    /**
     * Retrieves an existing session by its identifier.
     *
     * @param sessionId the session identifier
     * @return HTTP 200 with session details
     * @throws SessionNotFoundException if no session with the given id exists
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionDto> getSession(@PathVariable String sessionId) {
        Optional<Session> session = sessionStore.findById(sessionId);
        return session
                .map(s -> ResponseEntity.ok(DtoMapper.toSessionDto(s)))
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
}
