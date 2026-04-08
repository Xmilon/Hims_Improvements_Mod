package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.input.KeyboardInput;
import net.xmilon.himproveme.client.WardenPerkClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rewrites the local movement inputs after vanilla polling so stunned and frenzy control pressure can stay client-side.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputWardenEffectMixin {
    /**
     * Applies key remapping or axis inversion using the latest server-synced affliction snapshot.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$applyWardenMovementDisruption(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        WardenPerkClientHelper.applyMovementDisruption((KeyboardInput) (Object) this);
    }
}
