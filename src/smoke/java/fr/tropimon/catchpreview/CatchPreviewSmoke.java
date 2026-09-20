package fr.tropimon.catchpreview;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.tropimon.catchpreview.smokemixin.SmokeMouseAccess;
import java.nio.file.Files;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;

/** Real client, synthetic Pokémon and a newly created world; no external server. */
public final class CatchPreviewSmoke implements ClientModInitializer {
    private int stage = -2, tick, scale = 1;
    private long started;
    private UUID pokemonId;
    private Pokemon pokemon;
    private BlockPos pc;
    private boolean resized;
    private int savedX, savedY;

    @Override public void onInitializeClient() {
        if (!Boolean.getBoolean("catchpreview.smoke")) return;
        started = System.nanoTime();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                if (stage == 9) return;
                if (System.nanoTime() - started > 240_000_000_000L) throw new AssertionError("Offline smoke timeout stage=" + stage);
                if (client.getOverlay() != null || ++tick < 20) return;
                tick = 0;
                advance(client);
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("CATCH_GUI_SMOKE_FAILED");
                System.exit(1);
            }
        });
    }

    private void advance(MinecraftClient client) throws Exception {
        switch (stage) {
            case -2 -> {
                client.options.pauseOnLostFocus = false;
                client.options.getViewDistance().setValue(2);
                client.createIntegratedServerLoader().createAndStart("catch-preview-smoke-" + System.currentTimeMillis(),
                        new LevelInfo("Catch Preview verification", GameMode.CREATIVE, false, Difficulty.PEACEFUL,
                                true, new GameRules(), DataConfiguration.SAFE_MODE), new GeneratorOptions(1L, false, false),
                        registries -> registries.get(RegistryKeys.WORLD_PRESET).get(WorldPresets.FLAT).createDimensionsRegistryHolder(),
                        client.currentScreen);
                stage++;
            }
            case -1 -> {
                if (client.player == null || !client.world.isChunkLoaded(client.player.getBlockPos())) return;
                pc = client.player.getBlockPos().add(1, 0, 0);
                var id = client.player.getUuid();
                client.getServer().submit(() -> {
                    var player = client.getServer().getPlayerManager().getPlayer(id);
                    player.getServerWorld().setBlockState(pc, CobblemonBlocks.PC.getDefaultState(), 3);
                    var value = PokemonProperties.Companion.parse("pikachu level=100 shiny=true ability=lightningrod").create(player);
                    value.setOriginalTrainer(id);
                    value.getIvs().set(Stats.HP, 31);
                    value.getIvs().set(Stats.ATTACK, 0);
                    value.getIvs().set(Stats.DEFENCE, 10);
                    value.getIvs().set(Stats.SPECIAL_ATTACK, 11);
                    value.getIvs().set(Stats.SPECIAL_DEFENCE, 21);
                    value.getIvs().set(Stats.SPEED, 30);
                    var mark = com.cobblemon.mod.common.api.mark.Marks.all().getFirst();
                    value.setMarks(new java.util.HashSet<>(java.util.List.of(mark)));
                    value.setActiveMark(mark);
                    pokemonId = value.getUuid();
                    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
                    // The real mod correctly refuses to release the last party member.
                    party.set(new PartyPosition(1), PokemonProperties.Companion.parse("eevee level=25").create(player));
                    party.set(new PartyPosition(0), value);
                }).get();
                stage++;
            }
            case 0 -> {
                var party = CobblemonClient.INSTANCE.getStorage().getParty();
                if (party == null || (pokemon = party.findByUUID(pokemonId)) == null
                        || !client.world.getBlockState(pc).isOf(CobblemonBlocks.PC)) return;
                check(PokemonReleaseController.canRelease(pokemon), "fixture near PC and stored");
                client.options.getGuiScale().setValue(scale);
                client.onResolutionChanged();
                client.setScreen(new ChatScreen(""));
                // Reopen the same synthetic capture without changing capture-history production rules.
                var show = CatchPreviewState.class.getDeclaredMethod("show", Pokemon.class);
                show.setAccessible(true);
                show.invoke(null, pokemon);
                stage++;
            }
            case 1 -> {
                check(client.getWindow().getScaleFactor() == client.getWindow().calculateScaleFactor(scale, client.forcesUnicodeFont()), "effective scale");
                bounds(client);
                capture(client, "panel");
                pointer(client, left(client) + PreviewMarks.OFFSET_X + 3, top(client) + PreviewMarks.OFFSET_Y + 3);
                stage++;
            }
            case 2 -> {
                capture(client, "marks");
                int x = left(client), y = top(client);
                pointer(client, x + 15, y + 10);
                button(client, 1);
                pointer(client, -100, -100);
                button(client, 0);
                check(left(client) == 2 && top(client) == 2, "drag clamps top left through mouse mixin");
                pointer(client, 17, 12);
                button(client, 1);
                pointer(client, client.getWindow().getScaledWidth() + 100, client.getWindow().getScaledHeight() + 100);
                button(client, 0);
                bounds(client);
                check(left(client) == client.getWindow().getScaledWidth() - CatchPreviewRenderer.WIDTH - 2, "right drag bound");
                pointer(client, left(client) + CatchPreviewRenderer.WIDTH - 35, top(client) + 10);
                button(client, 1); button(client, 0);
                check(CatchPreviewState.releaseConfirmationActive(), "release first click only confirms");
                check(CobblemonClient.INSTANCE.getStorage().getParty().findByUUID(pokemonId) != null, "confirmation did not release");
                stage++;
            }
            case 3 -> {
                capture(client, "confirmation");
                savedX = left(client); savedY = top(client);
                pointer(client, left(client) + CatchPreviewRenderer.WIDTH - 12, top(client) + 10);
                button(client, 1); button(client, 0);
                check(CatchPreviewState.visible() == null && !CatchPreviewState.releaseConfirmationActive(), "close cancels safely");
                var show = CatchPreviewState.class.getDeclaredMethod("show", Pokemon.class);
                show.setAccessible(true);
                show.invoke(null, pokemon);
                check(left(client) == savedX && top(client) == savedY, "reopen retains last position");
                client.setScreen(null);
                stage++;
            }
            case 4 -> {
                capture(client, "hud");
                client.options.hudHidden = true;
                stage++;
            }
            case 5 -> {
                capture(client, "hidden");
                check(CatchPreviewState.visible() != null, "hidden HUD keeps capture state");
                client.options.hudHidden = false;
                client.setScreen(new ChatScreen(""));
                pointer(client, 1, 1);
                stage++;
            }
            case 6 -> {
                capture(client, "restored");
                System.out.println("CATCH_GUI_OK requested=" + scale + " effective=" + client.getWindow().getScaleFactor()
                        + " viewport=" + client.getWindow().getScaledWidth() + "x" + client.getWindow().getScaledHeight()
                        + " render,marks,drag,clamp,confirm,cancel,reopen,hud,hidden,restored resized=" + resized);
                if (!resized && ++scale <= 4) stage = 0;
                else if (!resized) {
                    resized = true;
                    scale = 4;
                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.getWindow().getHandle(), 960, 720);
                    stage = 0;
                }
                else {
                    System.out.println("CATCH_GUI_SMOKE_OK");
                    stage = 9;
                    client.scheduleStop();
                }
            }
            default -> throw new AssertionError("Unexpected stage");
        }
    }

    private static int left(MinecraftClient c) { return CatchPreviewRenderer.left(c.getWindow().getScaledWidth()); }
    private static int top(MinecraftClient c) { return CatchPreviewRenderer.top(c.getWindow().getScaledHeight()); }
    private static void bounds(MinecraftClient c) {
        check(left(c) >= 0 && top(c) >= 0 && left(c) + CatchPreviewRenderer.WIDTH <= c.getWindow().getScaledWidth()
                && top(c) + CatchPreviewRenderer.HEIGHT <= c.getWindow().getScaledHeight(), "full card in viewport");
    }
    private static void pointer(MinecraftClient c, double x, double y) {
        ((SmokeMouseAccess) c.mouse).catchSmoke$cursor(c.getWindow().getHandle(),
                x * c.getWindow().getWidth() / c.getWindow().getScaledWidth(),
                y * c.getWindow().getHeight() / c.getWindow().getScaledHeight());
    }
    private static void button(MinecraftClient c, int action) {
        ((SmokeMouseAccess) c.mouse).catchSmoke$button(c.getWindow().getHandle(), 0, action, 0);
    }
    private void capture(MinecraftClient c, String name) throws Exception {
        var folder = c.runDirectory.toPath().resolve("verification");
        Files.createDirectories(folder);
        try (var image = ScreenshotRecorder.takeScreenshot(c.getFramebuffer())) {
            image.writeTo(folder.resolve("catch-" + (resized ? "resized-" : "") + "gui" + scale + "-" + name + ".png"));
        }
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
