package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import net.xmilon.himproveme.leveling.LevelingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
public abstract class MerchantScreenHandlerLevelingMixin {
    @Shadow
    @Final
    private Merchant merchant;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/village/Merchant;)V", at = @At("TAIL"))
    private void himproveme$applyTradeScaling(int syncId, PlayerInventory playerInventory, Merchant merchant, CallbackInfo ci) {
        if (playerInventory.player instanceof ServerPlayerEntity serverPlayer) {
            LevelingManager.applyMerchantScaling(serverPlayer, this.merchant);
        }
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    private void himproveme$clearTradeScaling(PlayerEntity player, CallbackInfo ci) {
        LevelingManager.clearMerchantScaling(this.merchant);
    }
}
