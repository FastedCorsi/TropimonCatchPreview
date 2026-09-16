package fr.tropimon.catchpreview;

import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket;
import com.cobblemon.mod.common.net.messages.server.storage.party.*;
import com.cobblemon.mod.common.net.messages.server.storage.pc.*;

/** Observe only official protocol data, never the sender's implementation or screen. */
public final class StorageTransfers {
    private StorageTransfers() {}

    public static void sending(Object packet) {
        if (packet instanceof MovePCPokemonToPartyPacket p) CatchPreviewState.remember(p.getPokemonID());
        else if (packet instanceof MovePartyPokemonToPCPacket p) CatchPreviewState.remember(p.getPokemonID());
        else if (packet instanceof MovePCPokemonPacket p) CatchPreviewState.remember(p.getPokemonID());
        else if (packet instanceof MovePartyPokemonPacket p) CatchPreviewState.remember(p.getPokemonID());
        else if (packet instanceof SwapPCPartyPokemonPacket p) {
            CatchPreviewState.remember(p.getPartyPokemonID());
            CatchPreviewState.remember(p.getPcPokemonID());
        } else if (packet instanceof SwapPCPokemonPacket p) {
            CatchPreviewState.remember(p.getPokemon1ID());
            CatchPreviewState.remember(p.getPokemon2ID());
        } else if (packet instanceof SwapPartyPokemonPacket p) {
            CatchPreviewState.remember(p.getPokemon1ID());
            CatchPreviewState.remember(p.getPokemon2ID());
        }
    }
}
