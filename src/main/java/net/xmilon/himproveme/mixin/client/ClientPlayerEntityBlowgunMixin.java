package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityBlowgunMixin {
    @Redirect(
            method = "tickMovement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean himproveme$skipUseSlowdownWhileAimingBlowgun(ClientPlayerEntity player) {
        return player.isUsingItem() && !BlowgunItem.isBlowgun(player.getActiveItem());
    }
}
