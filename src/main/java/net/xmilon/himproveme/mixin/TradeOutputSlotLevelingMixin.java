package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import net.xmilon.himproveme.leveling.LevelingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeOutputSlot.class)
public abstract class TradeOutputSlotLevelingMixin {
    @Shadow
    @Final
    private MerchantInventory merchantInventory;

    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void himproveme$rewardTradeProgress(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        TradeOffer offer = this.merchantInventory.getTradeOffer();
        if (offer != null) {
            LevelingManager.onTradeCompleted(serverPlayer, offer);
        }
    }
}
