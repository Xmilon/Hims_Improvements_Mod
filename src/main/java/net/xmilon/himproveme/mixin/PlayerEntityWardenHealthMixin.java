package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the shared Warden-perk max-health penalty synchronized on both sides without persisting extra player data.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityWardenHealthMixin {
    /**
     * Updates the max-health modifier every tick so toggling perks or switching loadouts immediately updates the heart bar.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$applyWardenHealthPenalty(CallbackInfo ci) {
        WardenPerkHelper.applyHealthPenalty((PlayerEntity) (Object) this);
    }
}
