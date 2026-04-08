package net.xmilon.himproveme.mixin;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.SculkInvisibilityContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerSculkInvisibilityMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"))
    private void himproveme$beginPlayerActionSuppression(PlayerActionC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onPlayerAction", at = @At("RETURN"))
    private void himproveme$endPlayerActionSuppression(PlayerActionC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "onPlayerInteractBlock", at = @At("HEAD"))
    private void himproveme$beginBlockInteractionSuppression(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onPlayerInteractBlock", at = @At("RETURN"))
    private void himproveme$endBlockInteractionSuppression(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"))
    private void himproveme$beginItemInteractionSuppression(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onPlayerInteractItem", at = @At("RETURN"))
    private void himproveme$endItemInteractionSuppression(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"))
    private void himproveme$beginEntityInteractionSuppression(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onPlayerInteractEntity", at = @At("RETURN"))
    private void himproveme$endEntityInteractionSuppression(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "onClickSlot", at = @At("HEAD"))
    private void himproveme$beginSlotClickSuppression(ClickSlotC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onClickSlot", at = @At("RETURN"))
    private void himproveme$endSlotClickSuppression(ClickSlotC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    @Inject(method = "onCreativeInventoryAction", at = @At("HEAD"))
    private void himproveme$beginCreativeInventorySuppression(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "onCreativeInventoryAction", at = @At("RETURN"))
    private void himproveme$endCreativeInventorySuppression(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    private void himproveme$pushSuppression() {
        if (PerkAccess.hasSculkInvisibility(this.player)) {
            SculkInvisibilityContext.push(this.player);
        }
    }

    private void himproveme$popSuppression() {
        if (PerkAccess.hasSculkInvisibility(this.player)) {
            SculkInvisibilityContext.pop(this.player);
        }
    }
}
