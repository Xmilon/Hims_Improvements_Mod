package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.xmilon.himproveme.leveling.ClientLevelingState;
import net.xmilon.himproveme.leveling.LevelingManager;
import net.xmilon.himproveme.util.HimColorPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenLevelingMixin extends HandledScreen<PlayerScreenHandler> {
    private InventoryScreenLevelingMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void himproveme$renderLevelingInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ClientLevelingState.Snapshot snapshot = ClientLevelingState.getSnapshot();
        Text label = Text.literal("Level " + snapshot.level());
        int labelX = this.x + 8;
        int labelY = this.y + this.backgroundHeight + 4;
        int color = snapshot.enabled()
                ? HimColorPresets.get(HimColorPresets.SKY)
                : HimColorPresets.get(HimColorPresets.SILVER);

        context.drawText(this.textRenderer, label, labelX, labelY, color, false);

        int textWidth = this.textRenderer.getWidth(label);
        if (mouseX < labelX || mouseX > labelX + textWidth || mouseY < labelY || mouseY > labelY + 10) {
            return;
        }

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal(snapshot.enabled() ? "Leveling: ON" : "Leveling: OFF"));
        if (snapshot.isMaxLevel()) {
            tooltip.add(Text.literal("Level XP: MAX"));
        } else {
            tooltip.add(Text.literal("Level XP: " + snapshot.currentLevelXp() + " / " + snapshot.nextLevelXp()));
        }
        tooltip.add(Text.literal("Total Level XP: " + snapshot.totalLevelingXp()));
        tooltip.add(Text.literal("Play Time: " + LevelingManager.formatPlayTime(snapshot.totalPlayTicks())));
        context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
    }
}
