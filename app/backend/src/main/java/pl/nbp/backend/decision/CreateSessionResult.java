package pl.nbp.backend.decision;

import pl.nbp.backend.domain.Decision;

import java.time.Instant;

/**
 * The result of a successful {@link DecisionOrchestrator#createSession} call.
 *
 * <p>Carries the new session identifier, the structured AI decision, the
 * pre-composed first assistant message in Polish, and the session creation
 * timestamp.
 *
 * @param sessionId    the newly created session's opaque identifier
 * @param decision     the structured decision produced by the decision pipeline
 * @param firstMessage the pre-composed first assistant message in Polish
 * @param createdAt    the UTC timestamp when the session was created
 */
public record CreateSessionResult(
        String sessionId,
        Decision decision,
        String firstMessage,
        Instant createdAt) {
}
