package net.xmilon.himproveme.tooltip.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.item.custom.ContainerPreviewTooltipData;

@Environment(EnvType.CLIENT)
public final class ContainerPreviewTooltipComponent implements TooltipComponent {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/background");
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/bundle/slot");
    private static final Identifier BLOCKED_TEXTURE = Identifier.of(HimProveMe.MOD_ID, "textures/gui/access_blocked.png");
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int BLOCKED_TEXTURE_WIDTH = 100;
    private static final int BLOCKED_TEXTURE_HEIGHT = 80;
    private static final int BLOCKED_ICON_WIDTH = BLOCKED_TEXTURE_WIDTH / 2;
    private static final int BLOCKED_ICON_HEIGHT = BLOCKED_TEXTURE_HEIGHT / 2;
    private static final int BLOCKED_WIDTH = BLOCKED_ICON_WIDTH;
    private static final int BLOCKED_HEIGHT = BLOCKED_ICON_HEIGHT;

    private final ContainerPreviewTooltipData data;

    public ContainerPreviewTooltipComponent(ContainerPreviewTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        if (this.data.blocked()) {
            return BLOCKED_HEIGHT;
        }

        return this.data.rows() * SLOT_HEIGHT + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        if (this.data.blocked()) {
            return BLOCKED_WIDTH;
        }

        return this.data.columns() * SLOT_WIDTH + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        if (this.data.blocked()) {
            context.drawTexture(
                    BLOCKED_TEXTURE,
                    x,
                    y,
                    BLOCKED_ICON_WIDTH,
                    BLOCKED_ICON_HEIGHT,
                    0.0f,
                    0.0f,
                    BLOCKED_TEXTURE_WIDTH,
                    BLOCKED_TEXTURE_HEIGHT,
                    BLOCKED_TEXTURE_WIDTH,
                    BLOCKED_TEXTURE_HEIGHT
            );
            return;
        }

        int width = getWidth(textRenderer);
        int height = getHeight();
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height);

        for (int row = 0; row < this.data.rows(); row++) {
            for (int column = 0; column < this.data.columns(); column++) {
                int slotIndex = column + row * this.data.columns();
                int slotX = x + column * SLOT_WIDTH + 1;
                int slotY = y + row * SLOT_HEIGHT + 1;
                context.drawGuiTexture(SLOT_TEXTURE, slotX, slotY, 0, SLOT_WIDTH, SLOT_HEIGHT);

                if (slotIndex >= this.data.stacks().size()) {
                    continue;
                }

                ItemStack stack = this.data.stacks().get(slotIndex);
                if (stack.isEmpty()) {
                    continue;
                }

                context.drawItem(stack, slotX + 1, slotY + 1, slotIndex);
                context.drawItemInSlot(textRenderer, stack, slotX + 1, slotY + 1);
            }
        }
    }
}
