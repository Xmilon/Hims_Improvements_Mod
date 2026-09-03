package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityWardenAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void himproveme$prepareAttackBalance(Entity target, CallbackInfo ci) {
    }
}
