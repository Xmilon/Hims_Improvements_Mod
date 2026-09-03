package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.HungerManager;
import net.xmilon.himproveme.effect.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rapidly drains hunger and saturation while the player has the bleeding effect.
 * The per-tick drain itself is handled inside BleedingStatusEffect; this mixin
 * prevents the food bar from regenerating health while bleeding is active.
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerBleedingMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void himproveme$cancelRegenWhileBleeding(PlayerEntity player, CallbackInfo ci) {
        if (player.hasStatusEffect(ModStatusEffects.BLEEDING)) {
            ci.cancel();
        }
    }
}
