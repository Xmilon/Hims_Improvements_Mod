package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.access.BundleContentsLevelAccess;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BundleTooltipComponent.class)
public abstract class BundleTooltipComponentMixin {
    private static final Identifier HIMPROVEME_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/background");
    private static final Identifier HIMPROVEME_SLOT_TEXTURE = Identifier.ofVanilla("container/bundle/slot");
    private static final Identifier HIMPROVEME_BLOCKED_SLOT_TEXTURE = Identifier.ofVanilla("container/bundle/blocked_slot");

    @Shadow
    @Final
    private BundleContentsComponent bundleContents;

    @Inject(method = "drawItems", at = @At("HEAD"), cancellable = true)
    private void himproveme$drawExpandedBundleTooltip(TextRenderer textRenderer, int x, int y, DrawContext context, CallbackInfo ci) {
        int columns = Math.max(2, (int) Math.ceil(Math.sqrt(this.bundleContents.size() + 1.0)));
        int rows = (int) Math.ceil((this.bundleContents.size() + 1.0) / columns);
        int width = columns * 18 + 2;
        int height = rows * 20 + 2;
        int level = ((BundleContentsLevelAccess) (Object) this.bundleContents).himproveme$getBundleLevel();
        Fraction capacity = BundleUpgradeHelper.getCapacityFraction(level);
        boolean isFull = this.bundleContents.getOccupancy().compareTo(capacity) >= 0;
        int index = 0;

        context.drawGuiTexture(HIMPROVEME_BACKGROUND_TEXTURE, x, y, width, height);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slotX = x + column * 18 + 1;
                int slotY = y + row * 20 + 1;
                this.himproveme$drawSlot(slotX, slotY, index++, isFull, context, textRenderer);
            }
        }

        ci.cancel();
    }

    private void himproveme$drawSlot(int x, int y, int index, boolean shouldBlock, DrawContext context, TextRenderer textRenderer) {
        if (index >= this.bundleContents.size()) {
            context.drawGuiTexture(shouldBlock ? HIMPROVEME_BLOCKED_SLOT_TEXTURE : HIMPROVEME_SLOT_TEXTURE, x, y, 0, 18, 20);
            return;
        }

        ItemStack itemStack = this.bundleContents.get(index);
        context.drawGuiTexture(HIMPROVEME_SLOT_TEXTURE, x, y, 0, 18, 20);
        context.drawItem(itemStack, x + 1, y + 1, index);
        context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1);
        if (index == 0) {
            HandledScreen.drawSlotHighlight(context, x + 1, y + 1, 0);
        }
    }
}
