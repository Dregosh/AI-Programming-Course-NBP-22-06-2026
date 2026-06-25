package pl.nbp.backend.session;

import pl.nbp.backend.domain.Session;
import java.util.Optional;

/**
 * Port for storing and retrieving in-memory sessions.
 *
 * <p>Implementations must be thread-safe. The default implementation is
 * {@link InMemorySessionStore}, which stores sessions in a concurrent map with
 * TTL and max-entry eviction. A persistent implementation can be substituted
 * later without touching callers (ADR-000 §8.3).
 */
public interface SessionStore {

    /**
     * Persists a new session or overwrites an existing one with the same id.
     *
     * @param session the session to store; must not be {@code null}
     */
    void save(Session session);

    /**
     * Returns the session with the given id, or {@link Optional#empty()} if the session
     * does not exist or has expired.
     *
     * @param sessionId the session identifier
     * @return the session wrapped in an {@link Optional}, or empty
     */
    Optional<Session> findById(String sessionId);

    /**
     * Removes the session with the given id. No-op if the id is unknown.
     *
     * @param sessionId the session identifier
     */
    void deleteById(String sessionId);
}
