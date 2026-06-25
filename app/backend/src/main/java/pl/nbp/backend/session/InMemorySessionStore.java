package pl.nbp.backend.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.nbp.backend.domain.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory implementation of {@link SessionStore}.
 *
 * <p>Sessions are stored in a {@link ConcurrentHashMap} and subject to two eviction policies:
 * <ul>
 *   <li><strong>TTL:</strong> sessions older than {@code session.ttl-hours} are treated as expired
 *       and silently removed on access.</li>
 *   <li><strong>Max-entry cap:</strong> when {@code session.max-entries} is reached, the oldest
 *       session (by insertion time) is evicted before the new one is added.</li>
 * </ul>
 *
 * <p>This implementation is appropriate for the MVP single-instance deployment. A
 * {@code PersistentSessionStore} can replace it later without touching callers (ADR-000 §8.3).
 */
@Component
public class InMemorySessionStore implements SessionStore {

    private final Map<String, SessionEntry> entries = new ConcurrentHashMap<>();
    private final int ttlHours;
    private final int maxEntries;

    /**
     * Spring-managed constructor; values are bound from {@code application.yml}.
     *
     * @param ttlHours   time-to-live in hours (default: 2)
     * @param maxEntries maximum number of live sessions (default: 500); oldest is evicted when exceeded
     */
    public InMemorySessionStore(
            @Value("${session.ttl-hours:2}") int ttlHours,
            @Value("${session.max-entries:500}") int maxEntries) {
        this.ttlHours = ttlHours;
        this.maxEntries = maxEntries;
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the store is at capacity, the oldest entry is evicted first.
     */
    @Override
    public void save(Session session) {
        if (entries.size() >= maxEntries) {
            evictOldest();
        }
        entries.put(session.getSessionId(), new SessionEntry(session, Instant.now()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>An expired entry is removed from the store on access and treated as absent.
     */
    @Override
    public Optional<Session> findById(String sessionId) {
        SessionEntry entry = entries.get(sessionId);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry)) {
            entries.remove(sessionId);
            return Optional.empty();
        }
        return Optional.of(entry.session());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteById(String sessionId) {
        entries.remove(sessionId);
    }

    /**
     * Returns {@code true} if the elapsed time since the entry was stored exceeds the configured TTL.
     *
     * @param entry the session entry to check
     * @return {@code true} if expired
     */
    private boolean isExpired(SessionEntry entry) {
        Duration elapsed = Duration.between(entry.storedAt(), Instant.now());
        Duration ttl = Duration.ofHours(ttlHours);
        return elapsed.compareTo(ttl) > 0;
    }

    /**
     * Removes the entry with the earliest {@code storedAt} timestamp.
     * No-op if the map is empty.
     */
    private void evictOldest() {
        entries.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().storedAt()))
                .map(Map.Entry::getKey)
                .ifPresent(entries::remove);
    }

    /**
     * Internal wrapper that pairs a {@link Session} with its insertion timestamp.
     *
     * @param session  the stored session
     * @param storedAt the instant at which the session was inserted
     */
    private record SessionEntry(Session session, Instant storedAt) {}
}
