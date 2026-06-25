package pl.nbp.backend.web;

import pl.nbp.backend.domain.ChatMessage;
import pl.nbp.backend.decision.CreateSessionResult;
import pl.nbp.backend.domain.Decision;
import pl.nbp.backend.domain.Session;
import pl.nbp.backend.web.dto.ChatMessageDto;
import pl.nbp.backend.web.dto.CreateSessionResponseDto;
import pl.nbp.backend.web.dto.DecisionDto;
import pl.nbp.backend.web.dto.SessionDto;

import java.util.List;

/**
 * Static utility class for mapping domain objects to web-layer DTOs.
 *
 * <p>All methods are stateless and pure — no Spring context required.
 */
public class DtoMapper {

    private DtoMapper() {}

    /**
     * Maps a {@link Decision} domain object to a {@link DecisionDto}.
     *
     * @param decision the domain decision
     * @return the response DTO
     */
    public static DecisionDto toDecisionDto(Decision decision) {
        return new DecisionDto(
                decision.outcome(),
                decision.binding(),
                decision.justification(),
                decision.nextSteps(),
                decision.ruleReferences());
    }

    /**
     * Maps a {@link CreateSessionResult} to a {@link CreateSessionResponseDto}.
     *
     * @param result the orchestrator result
     * @return the response DTO
     */
    public static CreateSessionResponseDto toCreateSessionResponse(CreateSessionResult result) {
        return new CreateSessionResponseDto(
                result.sessionId(),
                toDecisionDto(result.decision()),
                result.firstMessage(),
                result.createdAt());
    }

    /**
     * Maps a {@link Session} to a {@link SessionDto}.
     *
     * @param session the domain session
     * @return the response DTO
     */
    public static SessionDto toSessionDto(Session session) {
        List<ChatMessageDto> messageDtos = session.getMessages().stream()
                .map(DtoMapper::toChatMessageDto)
                .toList();
        return new SessionDto(
                session.getSessionId(),
                session.getDecision() != null ? toDecisionDto(session.getDecision()) : null,
                messageDtos,
                session.getCreatedAt());
    }

    /**
     * Maps a {@link ChatMessage} to a {@link ChatMessageDto}.
     *
     * @param message the domain chat message
     * @return the response DTO
     */
    public static ChatMessageDto toChatMessageDto(ChatMessage message) {
        return new ChatMessageDto(message.role(), message.content(), message.createdAt());
    }
}
