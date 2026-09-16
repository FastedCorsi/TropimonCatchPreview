package fr.tropimon.catchpreview;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Session-long history: never evict UUIDs, including removed or transferred Pokémon. */
final class CaptureHistory {
    private final Set<UUID> known = new HashSet<>();
    private boolean ready;
    void remember(UUID id) { if (id != null) known.add(id); }
    boolean storageSet(UUID previous, UUID incoming) {
        remember(previous);
        if (incoming == null) return false;
        boolean added = known.add(incoming);
        return previous == null && added && ready;
    }
    void ready() { ready = true; }
    int size() { return known.size(); }
    void reset() { known.clear(); ready = false; }
}
