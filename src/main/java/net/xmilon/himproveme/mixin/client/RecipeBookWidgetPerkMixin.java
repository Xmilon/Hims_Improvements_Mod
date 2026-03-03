package net.xmilon.himproveme.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.network.perk.PerkBookRequestSyncPayload;
import net.xmilon.himproveme.network.perk.PerkBookUpgradePayload;
import net.xmilon.himproveme.perk.ClientPerkBookState;
import net.xmilon.himproveme.perk.PerkBookState;
import net.xmilon.himproveme.perk.PerkDefinition;
import net.xmilon.himproveme.perk.PerkInstanceState;
import net.xmilon.himproveme.perk.PerkRegistry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetPerkMixin {
    @Shadow
    private int leftOffset;
    @Shadow
    private int parentWidth;
    @Shadow
    private int parentHeight;
    @Shadow
    protected MinecraftClient client;
    @Shadow
    protected AbstractRecipeScreenHandler<?, ?> craftingScreenHandler;
    @Shadow
    public abstract boolean isOpen();

    @Unique
    private static final int PANEL_WIDTH = 176;
    @Unique
    private static final int PANEL_HEIGHT = 166;
    @Unique
    private static final int SLOT_SIZE = 22;
    @Unique
    private static final int EXTRA_LEFT_SHIFT = 14;

    @Unique
    private static final int CONTENT_X_OFFSET = 8;
    @Unique
    private static final int CONTENT_Y_OFFSET = 32;
    @Unique
    private static final int CONTENT_WIDTH = PANEL_WIDTH - 16;
    @Unique
    private static final int CONTENT_HEIGHT = PANEL_HEIGHT - 40;
    @Unique
    private static final int ROW_HEIGHT = 72;
    @Unique
    private static final int CELL_WIDTH = 48;
    @Unique
    private static final int CELL_START_X = 18;
    @Unique
    private static final int PAN_MARGIN = 80;

    @Unique
    private static final Identifier PERK_BACKGROUND_TEXTURE = Identifier.of(HimProveMe.MOD_ID, "textures/gui/perk/perks.png");

    @Unique
    private static int himproveme$contentOffsetX = 0;
    @Unique
    private static int himproveme$contentOffsetY = 0;
    @Unique
    private boolean himproveme$draggingContent = false;
    @Unique
    private int himproveme$dragStartMouseX;
    @Unique
    private int himproveme$dragStartMouseY;
    @Unique
    private int himproveme$dragStartContentOffsetX;
    @Unique
    private int himproveme$dragStartContentOffsetY;

    @Inject(method = "setOpen", at = @At("TAIL"))
    private void himproveme$requestPerkSync(boolean opened, CallbackInfo ci) {
        if (opened && himproveme$isPerkBookMode()) {
            himproveme$contentOffsetX = 0;
            himproveme$contentOffsetY = 0;
            himproveme$draggingContent = false;
            ClientPlayNetworking.send(PerkBookRequestSyncPayload.INSTANCE);
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void himproveme$renderPerkBook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!himproveme$isPerkBookMode() || !isOpen()) {
            return;
        }

        int panelX = himproveme$panelX();
        int panelY = himproveme$panelY();
        int contentX = panelX + CONTENT_X_OFFSET;
        int contentY = panelY + CONTENT_Y_OFFSET;

        PerkBookState state = ClientPerkBookState.getSnapshot();
        PerkInstanceState instance = state.getSelectedInstance();
        Map<Integer, List<PerkDefinition>> perksByRow = himproveme$buildPerksByRow();
        List<Integer> rowIndexes = new ArrayList<>(perksByRow.keySet());
        rowIndexes.sort(Integer::compareTo);

        if (himproveme$draggingContent) {
            long handle = client.getWindow().getHandle();
            if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                himproveme$contentOffsetX = himproveme$dragStartContentOffsetX + (mouseX - himproveme$dragStartMouseX);
                himproveme$contentOffsetY = himproveme$dragStartContentOffsetY + (mouseY - himproveme$dragStartMouseY);
                himproveme$clampContentOffsets(perksByRow, rowIndexes.size());
            } else {
                himproveme$draggingContent = false;
            }
        }

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 100.0F);

        context.drawTexture(PERK_BACKGROUND_TEXTURE, panelX, panelY, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
        context.drawText(client.textRenderer, Text.translatable("perk.himproveme.title"), panelX + 8, panelY + 7, 0xFFE8D0A0, false);
        context.drawText(client.textRenderer, Text.translatable("perk.himproveme.unlock_cost", PerkRegistry.XP_LEVEL_COST_PER_UPGRADE), panelX + 8, panelY + 18, 0xFFD6B27C, false);

        context.enableScissor(contentX, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT);

        for (int visualRowIndex = 0; visualRowIndex < rowIndexes.size(); visualRowIndex++) {
            int rowId = rowIndexes.get(visualRowIndex);
            List<PerkDefinition> rowPerks = perksByRow.get(rowId);
            int rowBaseY = contentY + himproveme$contentOffsetY + visualRowIndex * ROW_HEIGHT;
            int rowTextY = rowBaseY;
            int slotY = rowBaseY + 14;
            int buttonY = slotY + SLOT_SIZE + 17;

            context.drawText(client.textRenderer, Text.translatable(himproveme$getCategoryKeyForRow(rowPerks)), contentX + himproveme$contentOffsetX, rowTextY, 0xFFE3C998, false);

            int maxColumn = himproveme$getMaxColumn(rowId, rowPerks);
            for (int col = 0; col <= maxColumn; col++) {
                int cellX = contentX + CELL_START_X + himproveme$contentOffsetX + col * CELL_WIDTH;
                PerkDefinition perk = himproveme$getPerkAtColumn(rowPerks, col);

                if (perk == null) {
                    if (himproveme$isArrowCell(rowId, col)) {
                        himproveme$drawArrowCell(context, cellX, slotY);
                    }
                    continue;
                }

                boolean unlocked = instance.getLevel(perk.id()) > 0;
                himproveme$drawPerkSlot(context, cellX, slotY, perk, unlocked);
                context.drawCenteredTextWithShadow(client.textRenderer, perk.name(), cellX + SLOT_SIZE / 2, slotY + SLOT_SIZE + 6, perk.titleColor());
                himproveme$drawUnlockButton(context, cellX - 10, buttonY, perk, instance);
            }
        }

        context.disableScissor();
        context.getMatrices().pop();
        ci.cancel();
    }

    @Inject(method = "drawTooltip", at = @At("HEAD"), cancellable = true)
    private void himproveme$drawTooltip(DrawContext context, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        if (!himproveme$isPerkBookMode() || !isOpen()) {
            return;
        }

        PerkBookState state = ClientPerkBookState.getSnapshot();
        PerkInstanceState instance = state.getSelectedInstance();
        PerkDefinition hoveredPerk = himproveme$getHoveredPerk(mouseX, mouseY);
        if (hoveredPerk != null) {
            List<Text> lines = new ArrayList<>();
            for (Identifier requiredId : hoveredPerk.requiredPerkIds()) {
                if (instance.getLevel(requiredId) <= 0) {
                    PerkDefinition requiredPerk = PerkRegistry.get(requiredId);
                    Text requiredName = requiredPerk == null ? Text.literal(requiredId.toString()) : requiredPerk.name();
                    lines.add(Text.translatable("perk.himproveme.info.requires", requiredName));
                }
            }
            lines.add(hoveredPerk.name());
            lines.add(hoveredPerk.description());
            lines.add(Text.translatable("perk.himproveme.info.unlocks", hoveredPerk.unlockFunction()));
            context.drawTooltip(client.textRenderer, lines, mouseX, mouseY);
        }
        ci.cancel();
    }

    @Inject(method = "drawGhostSlots", at = @At("HEAD"), cancellable = true)
    private void himproveme$hideGhostSlots(DrawContext context, int x, int y, boolean notInventory, float delta, CallbackInfo ci) {
        if (himproveme$isPerkBookMode()) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!himproveme$isPerkBookMode() || !isOpen()) {
            return;
        }
        if (button != 0) {
            cir.setReturnValue(false);
            return;
        }

        int panelX = himproveme$panelX();
        int panelY = himproveme$panelY();
        int contentX = panelX + CONTENT_X_OFFSET;
        int contentY = panelY + CONTENT_Y_OFFSET;
        if (!himproveme$isInside(mouseX, mouseY, panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT)) {
            cir.setReturnValue(false);
            return;
        }

        PerkBookState state = ClientPerkBookState.getSnapshot();
        PerkInstanceState instance = state.getSelectedInstance();
        int selectedIndex = state.selectedIndex();

        Map<Integer, List<PerkDefinition>> perksByRow = himproveme$buildPerksByRow();
        List<Integer> rowIndexes = new ArrayList<>(perksByRow.keySet());
        rowIndexes.sort(Integer::compareTo);

        for (int visualRowIndex = 0; visualRowIndex < rowIndexes.size(); visualRowIndex++) {
            int rowId = rowIndexes.get(visualRowIndex);
            List<PerkDefinition> rowPerks = perksByRow.get(rowId);
            int rowBaseY = contentY + himproveme$contentOffsetY + visualRowIndex * ROW_HEIGHT;
            int slotY = rowBaseY + 14;
            int buttonY = slotY + SLOT_SIZE + 17;

            int maxColumn = himproveme$getMaxColumn(rowId, rowPerks);
            for (int col = 0; col <= maxColumn; col++) {
                PerkDefinition perk = himproveme$getPerkAtColumn(rowPerks, col);
                if (perk == null) {
                    continue;
                }

                int cellX = contentX + CELL_START_X + himproveme$contentOffsetX + col * CELL_WIDTH;
                int buttonX = cellX - 10;
                int buttonWidth = 46;
                int buttonHeight = 14;

                if (!himproveme$isInside(mouseX, mouseY, contentX, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT)) {
                    continue;
                }
                if (himproveme$isInside(mouseX, mouseY, buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight)) {
                    if (instance.getLevel(perk.id()) <= 0) {
                        ClientPlayNetworking.send(new PerkBookUpgradePayload(selectedIndex, perk.id()));
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (himproveme$isInside(mouseX, mouseY, contentX, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT)) {
            himproveme$draggingContent = true;
            himproveme$dragStartMouseX = (int) mouseX;
            himproveme$dragStartMouseY = (int) mouseY;
            himproveme$dragStartContentOffsetX = himproveme$contentOffsetX;
            himproveme$dragStartContentOffsetY = himproveme$contentOffsetY;
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void himproveme$blockRecipeBookKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (himproveme$isPerkBookMode() && isOpen()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void himproveme$blockRecipeBookTyping(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (himproveme$isPerkBookMode() && isOpen()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private Map<Integer, List<PerkDefinition>> himproveme$buildPerksByRow() {
        Map<Integer, List<PerkDefinition>> byRow = new HashMap<>();
        for (PerkDefinition definition : PerkRegistry.valuesOrderedByGrid()) {
            byRow.computeIfAbsent(definition.row(), ignored -> new ArrayList<>()).add(definition);
        }
        for (List<PerkDefinition> row : byRow.values()) {
            row.sort((a, b) -> Integer.compare(a.column(), b.column()));
        }
        return byRow;
    }

    @Unique
    private int himproveme$getMaxColumn(int rowId, List<PerkDefinition> rowPerks) {
        int max = 0;
        for (PerkDefinition perk : rowPerks) {
            if (perk.column() > max) {
                max = perk.column();
            }
        }
        int maxFromRegistry = PerkRegistry.getMaxColumnForRow(rowId);
        return Math.max(max, maxFromRegistry);
    }

    @Unique
    private String himproveme$getCategoryKeyForRow(List<PerkDefinition> rowPerks) {
        if (rowPerks.isEmpty()) {
            return "perk.himproveme.category.wip";
        }
        return rowPerks.get(0).categoryKey();
    }

    @Unique
    private @Nullable PerkDefinition himproveme$getPerkAtColumn(List<PerkDefinition> rowPerks, int column) {
        for (PerkDefinition perk : rowPerks) {
            if (perk.column() == column) {
                return perk;
            }
        }
        return null;
    }

    @Unique
    private boolean himproveme$isArrowCell(int row, int column) {
        return PerkRegistry.isArrowCell(row, column);
    }

    @Unique
    private void himproveme$drawPerkSlot(DrawContext context, int x, int y, PerkDefinition definition, boolean unlocked) {
        int border = unlocked ? 0xFF5D9A44 : 0xFF7B4A3B;
        int fill = unlocked ? 0xFF2F4D29 : 0xFF40261F;
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, border);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, fill);

        Item iconItem = Registries.ITEM.get(definition.iconItemId());
        if (iconItem == Items.AIR) {
            iconItem = Items.BARRIER;
        }
        context.drawItem(new ItemStack(iconItem), x + 3, y + 3);
    }

    @Unique
    private void himproveme$drawArrowCell(DrawContext context, int x, int y) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF6A5639);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2B241A);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("->"), x + SLOT_SIZE / 2, y + 7, 0xFFF2C57D);
    }

    @Unique
    private void himproveme$drawUnlockButton(DrawContext context, int x, int y, PerkDefinition definition, PerkInstanceState instance) {
        boolean unlocked = instance.getLevel(definition.id()) > 0;
        boolean canUnlock = true;
        for (Identifier requiredId : definition.requiredPerkIds()) {
            if (instance.getLevel(requiredId) <= 0) {
                canUnlock = false;
                break;
            }
        }
        canUnlock = !unlocked && canUnlock;

        int width = 46;
        int height = 14;
        int bg = unlocked ? 0xFF2B6E39 : canUnlock ? 0xFF8B5E2F : 0xFF474747;
        context.fill(x, y, x + width, y + height, bg);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF1A1A1A);

        Text label = unlocked ? Text.translatable("perk.himproveme.unlocked") : canUnlock
                ? Text.translatable("perk.himproveme.unlock")
                : Text.translatable("perk.himproveme.locked");
        context.drawCenteredTextWithShadow(client.textRenderer, label, x + width / 2, y + 3, 0xFFFFFFFF);
    }

    @Unique
    private boolean himproveme$isPerkBookMode() {
        return craftingScreenHandler != null
                && craftingScreenHandler.getCategory() == RecipeBookCategory.CRAFTING
                && client != null
                && client.player != null;
    }

    @Unique
    private int himproveme$panelX() {
        return (parentWidth - PANEL_WIDTH) / 2 - leftOffset - EXTRA_LEFT_SHIFT;
    }

    @Unique
    private int himproveme$panelY() {
        return (parentHeight - PANEL_HEIGHT) / 2;
    }

    @Unique
    private void himproveme$clampContentOffsets(Map<Integer, List<PerkDefinition>> perksByRow, int rowCount) {
        int maxCells = 0;
        for (List<PerkDefinition> rowPerks : perksByRow.values()) {
            int rowId = rowPerks.isEmpty() ? 0 : rowPerks.get(0).row();
            int cells = himproveme$getMaxColumn(rowId, rowPerks) + 1;
            if (cells > maxCells) {
                maxCells = cells;
            }
        }

        int graphWidth = CELL_START_X + (Math.max(1, maxCells) - 1) * CELL_WIDTH + SLOT_SIZE + 18;
        int graphHeight = rowCount * ROW_HEIGHT;

        int minOffsetX = Math.min(0, CONTENT_WIDTH - graphWidth) - PAN_MARGIN;
        int maxOffsetX = PAN_MARGIN;
        int minOffsetY = Math.min(0, CONTENT_HEIGHT - graphHeight) - PAN_MARGIN;
        int maxOffsetY = PAN_MARGIN;

        if (himproveme$contentOffsetX < minOffsetX) {
            himproveme$contentOffsetX = minOffsetX;
        } else if (himproveme$contentOffsetX > maxOffsetX) {
            himproveme$contentOffsetX = maxOffsetX;
        }

        if (himproveme$contentOffsetY < minOffsetY) {
            himproveme$contentOffsetY = minOffsetY;
        } else if (himproveme$contentOffsetY > maxOffsetY) {
            himproveme$contentOffsetY = maxOffsetY;
        }
    }

    @Unique
    private boolean himproveme$isInside(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
    }

    @Unique
    private @Nullable PerkDefinition himproveme$getHoveredPerk(int mouseX, int mouseY) {
        int panelX = himproveme$panelX();
        int panelY = himproveme$panelY();
        int contentX = panelX + CONTENT_X_OFFSET;
        int contentY = panelY + CONTENT_Y_OFFSET;
        if (!himproveme$isInside(mouseX, mouseY, contentX, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT)) {
            return null;
        }

        Map<Integer, List<PerkDefinition>> perksByRow = himproveme$buildPerksByRow();
        List<Integer> rowIndexes = new ArrayList<>(perksByRow.keySet());
        rowIndexes.sort(Integer::compareTo);

        for (int visualRowIndex = 0; visualRowIndex < rowIndexes.size(); visualRowIndex++) {
            int rowId = rowIndexes.get(visualRowIndex);
            List<PerkDefinition> rowPerks = perksByRow.get(rowId);
            int slotY = contentY + himproveme$contentOffsetY + visualRowIndex * ROW_HEIGHT + 14;

            int maxColumn = himproveme$getMaxColumn(rowId, rowPerks);
            for (int col = 0; col <= maxColumn; col++) {
                PerkDefinition perk = himproveme$getPerkAtColumn(rowPerks, col);
                if (perk == null) {
                    continue;
                }
                int cellX = contentX + CELL_START_X + himproveme$contentOffsetX + col * CELL_WIDTH;
                if (himproveme$isInside(mouseX, mouseY, cellX, slotY, cellX + SLOT_SIZE, slotY + SLOT_SIZE)) {
                    return perk;
                }
            }
        }

        return null;
    }
}
