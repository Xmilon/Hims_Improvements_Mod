package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.xmilon.himproveme.perk.ClientVillagerTradeStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenVillagerPerkMixin extends HandledScreen<MerchantScreenHandler> {
    @Shadow
    private int selectedIndex;

    private MerchantScreenVillagerPerkMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void himproveme$drawVillagerTradeStatus(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        ClientVillagerTradeStatus.OfferStatus status = ClientVillagerTradeStatus.getStatus(this.handler.syncId, this.selectedIndex);
        if (status == null) {
            return;
        }

        Text remainingTrades = Text.translatable("perk.himproveme.villager_trades_remaining", status.remainingTrades());
        Text currentCooldown = status.cooldownLeftTicks() > 0L
                ? Text.literal(himproveme$formatDuration(status.cooldownLeftTicks()))
                : Text.translatable("perk.himproveme.villager_trade_ready");
        Text totalCooldown = Text.literal(himproveme$formatDuration(ClientVillagerTradeStatus.getTotalCooldownTicks(this.handler.syncId)));
        Text cooldownLine = Text.translatable("perk.himproveme.villager_trade_cooldown", currentCooldown, totalCooldown);

        context.drawText(this.textRenderer, remainingTrades, 8, 92, 0xFF3F8E2D, false);
        context.drawText(this.textRenderer, cooldownLine, 8, 104, 0xFF6E5940, false);
    }

    private String himproveme$formatDuration(long ticks) {
        long totalSeconds = Math.max(0L, ticks / 20L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return minutes > 0L ? hours + "h " + minutes + "m" : hours + "h";
        }
        if (minutes > 0L) {
            return seconds > 0L ? minutes + "m " + seconds + "s" : minutes + "m";
        }
        return seconds + "s";
    }
}
