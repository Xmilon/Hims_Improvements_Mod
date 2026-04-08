package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerDualWieldMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityAnimation", at = @At("HEAD"), cancellable = true)
    private void himproveme$skipLocalOffhandSwingReplay(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (packet.getAnimationId() != EntityAnimationS2CPacket.SWING_OFF_HAND
                || client.player == null
                || this.world == null) {
            return;
        }

        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (entity == client.player && DualWieldCombatHelper.isOffhandSwingActive(client.player)) {
            ci.cancel();
        }
    }
}
