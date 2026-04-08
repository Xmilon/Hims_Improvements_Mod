package net.xmilon.himproveme.mixin.client;

import net.xmilon.himproveme.client.WardenPerkClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Reduces mouse sensitivity while stunned by scaling the final look deltas just before they reach the player entity.
 */
@Mixin(net.minecraft.client.Mouse.class)
public abstract class MouseWardenEffectMixin {
    /**
     * Multiplies both look axes by the synced stunned sensitivity scalar.
     */
    @ModifyArgs(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private void himproveme$reduceMouseSensitivity(Args args) {
        double multiplier = WardenPerkClientHelper.getMouseLookMultiplier();
        args.set(0, args.<Double>get(0) * multiplier);
        args.set(1, args.<Double>get(1) * multiplier);
    }
}
