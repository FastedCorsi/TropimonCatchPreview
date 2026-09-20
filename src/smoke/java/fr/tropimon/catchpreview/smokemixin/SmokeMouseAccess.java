package fr.tropimon.catchpreview.smokemixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Routes input through the real mouse mixin, without moving the operating-system pointer. */
@Mixin(Mouse.class)
public interface SmokeMouseAccess {
    @Invoker("onCursorPos") void catchSmoke$cursor(long window, double x, double y);
    @Invoker("onMouseButton") void catchSmoke$button(long window, int button, int action, int modifiers);
}
