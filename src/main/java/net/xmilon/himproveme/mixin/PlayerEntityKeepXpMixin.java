package net.xmilon.himproveme.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.util.KeepXpManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityKeepXpMixin {
    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void himproveme$storeXpBeforeDeath(CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        KeepXpManager.store(serverPlayer);
    }
}