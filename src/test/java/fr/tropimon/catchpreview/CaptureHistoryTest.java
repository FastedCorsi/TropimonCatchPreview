package fr.tropimon.catchpreview;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CaptureHistoryTest {
    @Test void initialSyncAndPostBattleUpdatesAreNotCaptures() {
        var h = new CaptureHistory();
        UUID team = UUID.randomUUID();
        assertFalse(h.storageSet(null, team));
        h.ready();
        assertFalse(h.storageSet(team, team));
        assertFalse(h.storageSet(team, null));
        assertFalse(h.storageSet(null, team));
    }
    @Test void capturesInEmptyPartyOrPcSlotsRemainDetectedInRapidSuccession() {
        var h = new CaptureHistory(); h.ready();
        for (int i = 0; i < 20_000; i++) assertTrue(h.storageSet(null, new UUID(1, i)));
        assertEquals(20_000, h.size());
        assertFalse(h.storageSet(null, new UUID(1, 0)), "No arbitrary UUID eviction");
    }
    @Test void teamTransfersAndBulkSyncDoNotSuppressConcurrentNewCaptures() {
        var h = new CaptureHistory(); h.ready();
        UUID transferred = UUID.randomUUID(), pcSync = UUID.randomUUID();
        h.remember(transferred); h.remember(pcSync);
        assertFalse(h.storageSet(null, transferred));
        assertTrue(h.storageSet(null, UUID.randomUUID()));
        assertFalse(h.storageSet(null, pcSync));
    }
    @Test void occupiedSlotReplacementIsRememberedButNotShown() {
        var h = new CaptureHistory(); h.ready();
        UUID old = UUID.randomUUID(), replacement = UUID.randomUUID();
        assertFalse(h.storageSet(old, replacement));
        assertFalse(h.storageSet(null, old));
        assertFalse(h.storageSet(null, replacement));
    }
    @Test void sessionResetWaitsForInitialSyncAgain() {
        var h = new CaptureHistory(); h.ready(); h.remember(UUID.randomUUID());
        h.reset();
        assertEquals(0, h.size());
        assertFalse(h.storageSet(null, UUID.randomUUID()));
        h.ready();
        assertTrue(h.storageSet(null, UUID.randomUUID()));
    }
}
