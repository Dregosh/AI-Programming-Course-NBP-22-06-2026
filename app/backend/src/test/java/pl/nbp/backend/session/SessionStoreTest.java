package pl.nbp.backend.session;

import org.junit.jupiter.api.Test;
import pl.nbp.backend.domain.ServiceRequest;
import pl.nbp.backend.domain.RequestType;
import pl.nbp.backend.domain.EquipmentCategory;
import pl.nbp.backend.domain.Session;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionStoreTest {

    private Session makeSession() {
        ServiceRequest req = new ServiceRequest(
                RequestType.RETURN, EquipmentCategory.SMARTPHONE,
                "iPhone 14", LocalDate.now().minusDays(10), null);
        return new Session(UUID.randomUUID().toString(), req, Instant.now());
    }

    @Test
    void saveThenFindReturnsSession() {
        InMemorySessionStore store = new InMemorySessionStore(2, 100);
        Session session = makeSession();
        store.save(session);
        Optional<Session> found = store.findById(session.getSessionId());
        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo(session.getSessionId());
    }

    @Test
    void expiredEntryReturnsEmpty() throws InterruptedException {
        // ttlHours=0 → Duration.ofHours(0) = ZERO; any non-zero elapsed time triggers expiry
        InMemorySessionStore store = new InMemorySessionStore(0, 100);
        Session session = makeSession();
        store.save(session);
        Thread.sleep(5); // ensure some time passes
        Optional<Session> found = store.findById(session.getSessionId());
        assertThat(found).isEmpty();
    }

    @Test
    void unknownIdReturnsEmpty() {
        InMemorySessionStore store = new InMemorySessionStore(2, 100);
        assertThat(store.findById("no-such-id")).isEmpty();
    }

    @Test
    void exceedingMaxEntriesEvictsOldest() throws InterruptedException {
        InMemorySessionStore store = new InMemorySessionStore(2, 2);
        Session s1 = makeSession();
        store.save(s1);
        Thread.sleep(5); // ensure s1 is older
        Session s2 = makeSession();
        store.save(s2);
        // both fit
        assertThat(store.findById(s1.getSessionId())).isPresent();
        assertThat(store.findById(s2.getSessionId())).isPresent();
        // adding s3 should evict s1 (oldest)
        Session s3 = makeSession();
        store.save(s3);
        assertThat(store.findById(s1.getSessionId())).isEmpty(); // evicted
        assertThat(store.findById(s2.getSessionId())).isPresent();
        assertThat(store.findById(s3.getSessionId())).isPresent();
    }
}
