package fr.tropimon.catchpreview;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.client.CobblemonClient;

import java.util.UUID;

public final class CatchPreviewState {
    private static final CaptureHistory HISTORY = new CaptureHistory();
    private static final ConfirmationQueue<Pokemon> PREVIEW = new ConfirmationQueue<>();

    private CatchPreviewState() {}

    public static synchronized void storageSet(Pokemon previous, Pokemon pokemon) {
        boolean captured = HISTORY.storageSet(previous == null ? null : previous.getUuid(),
                pokemon == null ? null : pokemon.getUuid());
        if (pokemon == null) return;
        // This runs before the destination write, so a Pokémon already in another
        // store is a transfer even when no outgoing move packet was observed.
        if (captured && !alreadyStored(pokemon.getUuid())) show(pokemon);
        if (PREVIEW.visible != null && PREVIEW.visible.getUuid().equals(pokemon.getUuid())) PREVIEW.visible = pokemon;
        if (PREVIEW.queued != null && PREVIEW.queued.getUuid().equals(pokemon.getUuid())) PREVIEW.queued = pokemon;
    }

    private static boolean alreadyStored(UUID id) {
        var storage = CobblemonClient.INSTANCE.getStorage();
        var party = storage.getParty();
        if (party != null && party.findByUUID(id) != null) return true;
        for (var pc : storage.getPcStores().values()) if (pc.findByUUID(id) != null) return true;
        return false;
    }

    public static synchronized Pokemon visible() {
        return PREVIEW.visible;
    }

    public static synchronized void tick() {
        if (PREVIEW.expire()) PreviewPosition.stopDrag();
    }

    public static synchronized void remember(UUID id) {
        HISTORY.remember(id);
    }

    public static synchronized void close() {
        PreviewPosition.stopDrag();
        PREVIEW.close();
    }

    private static void show(Pokemon pokemon) {
        boolean queued = PREVIEW.pending != null;
        PREVIEW.show(pokemon);
        if (queued) return;
        TropimonCatchPreviewClient.LOGGER.info("Aperçu affiché pour {} ({})",
                pokemon.getSpecies().getName(), pokemon.getUuid());
    }

    public static synchronized void storageReady() {
        HISTORY.ready();
        TropimonCatchPreviewClient.LOGGER.debug("Synchronisation initiale du stockage terminée ({} Pokémon connus)", HISTORY.size());
    }

    public static synchronized void beginSession() {
        PokemonReleaseController.reset();
        PokemonPortraitRenderer.reset();
        PreviewPosition.stopDrag();
        HISTORY.reset();
        PREVIEW.close();
    }

    public static synchronized void endSession() {
        beginSession();
    }

    public static synchronized boolean click(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        tick();
        if (PREVIEW.visible == null) return false;
        int left = CatchPreviewRenderer.left(screenWidth);
        int top = CatchPreviewRenderer.top(screenHeight);
        if (mouseX >= left + CatchPreviewRenderer.WIDTH - 18 && mouseX < left + CatchPreviewRenderer.WIDTH - 4
                && mouseY >= top + 3 && mouseY < top + 18) {
            close();
            return true;
        }
        if (mouseX >= left + CatchPreviewRenderer.WIDTH - 48 && mouseX < left + CatchPreviewRenderer.WIDTH - 21
                && mouseY >= top + 5 && mouseY < top + 19
                && PokemonReleaseController.canRelease(PREVIEW.visible)) {
            if (PREVIEW.pending != null) {
                Pokemon target = PREVIEW.pending;
                boolean released = PokemonReleaseController.release(target);
                PREVIEW.complete(released);
            } else {
                PREVIEW.confirm();
            }
            return true;
        }
        return false;
    }

    public static synchronized boolean releaseConfirmationActive() {
        return PREVIEW.pending != null;
    }
}
