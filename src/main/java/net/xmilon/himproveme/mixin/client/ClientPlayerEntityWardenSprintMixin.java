package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.xmilon.himproveme.client.WardenPerkClientHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks sprint start and clears active sprinting while stunned so the control impairment is visible immediately.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityWardenSprintMixin {
    /**
     * Stops an already-sprinting player before the rest of movement logic runs when stunned.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void himproveme$cancelStunnedSprint(CallbackInfo ci) {
        if (WardenPerkClientHelper.shouldBlockSprint()) {
            ((ClientPlayerEntity) (Object) this).setSprinting(false);
        }
    }

    /**
     * Prevents vanilla from starting a new sprint while the stunned effect is active.
     */
    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void himproveme$blockSprintStart(CallbackInfoReturnable<Boolean> cir) {
        if (WardenPerkClientHelper.shouldBlockSprint()) {
            cir.setReturnValue(false);
        }
    }
}
