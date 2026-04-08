package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.client.CreativePerkBookClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenPerkButtonMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Unique
    private static final ButtonTextures HIMPROVEME_PERK_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of("minecraft", "recipe_book/button"),
            Identifier.of("minecraft", "recipe_book/button_highlighted")
    );
    @Unique
    private static final int HIMPROVEME_PERK_BUTTON_WIDTH = 20;
    @Unique
    private static final int HIMPROVEME_PERK_BUTTON_HEIGHT = 18;
    @Unique
    private static final int HIMPROVEME_PERK_BUTTON_SIDE_MARGIN = 4;
    @Unique
    private static final int HIMPROVEME_PERK_BUTTON_TOP_OFFSET = 8;

    @Unique
    private TexturedButtonWidget himproveme$perkButton;

    private CreativeInventoryScreenPerkButtonMixin(CreativeInventoryScreen.CreativeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Shadow
    public abstract boolean isInventoryTabSelected();

    @Inject(method = "init", at = @At("TAIL"))
    private void himproveme$addCreativePerkButton(CallbackInfo ci) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        this.himproveme$perkButton = this.addDrawableChild(new TexturedButtonWidget(
                himproveme$getPerkButtonX(),
                himproveme$getPerkButtonY(),
                HIMPROVEME_PERK_BUTTON_WIDTH,
                HIMPROVEME_PERK_BUTTON_HEIGHT,
                HIMPROVEME_PERK_BUTTON_TEXTURES,
                button -> himproveme$openPerkBook(),
                Text.empty()
        ));
        this.himproveme$perkButton.setTooltip(Tooltip.of(Text.translatable("perk.himproveme.open_book")));
        this.himproveme$perkButton.visible = this.isInventoryTabSelected();
        this.himproveme$perkButton.active = this.isInventoryTabSelected();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void himproveme$updateCreativePerkButtonVisibility(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.himproveme$perkButton == null) {
            return;
        }

        boolean visible = this.isInventoryTabSelected();
        this.himproveme$perkButton.setPosition(
                himproveme$getPerkButtonX(),
                himproveme$getPerkButtonY()
        );
        this.himproveme$perkButton.visible = visible;
        this.himproveme$perkButton.active = visible;
    }

    @Unique
    private int himproveme$getPerkButtonX() {
        int rightSideX = this.x + this.backgroundWidth + HIMPROVEME_PERK_BUTTON_SIDE_MARGIN;
        int maxX = this.width - HIMPROVEME_PERK_BUTTON_WIDTH - HIMPROVEME_PERK_BUTTON_SIDE_MARGIN;
        if (rightSideX <= maxX) {
            return rightSideX;
        }

        return Math.max(
                HIMPROVEME_PERK_BUTTON_SIDE_MARGIN,
                this.x - HIMPROVEME_PERK_BUTTON_WIDTH - HIMPROVEME_PERK_BUTTON_SIDE_MARGIN
        );
    }

    @Unique
    private int himproveme$getPerkButtonY() {
        return MathHelper.clamp(
                this.y + HIMPROVEME_PERK_BUTTON_TOP_OFFSET,
                HIMPROVEME_PERK_BUTTON_SIDE_MARGIN,
                this.height - HIMPROVEME_PERK_BUTTON_HEIGHT - HIMPROVEME_PERK_BUTTON_SIDE_MARGIN
        );
    }

    @Unique
    private void himproveme$openPerkBook() {
        if (this.client == null || this.client.player == null || this.client.getNetworkHandler() == null) {
            return;
        }

        boolean filtering = this.client.player.getRecipeBook().isFilteringCraftable(RecipeBookCategory.CRAFTING);
        this.client.player.getRecipeBook().setGuiOpen(RecipeBookCategory.CRAFTING, true);
        this.client.getNetworkHandler().sendPacket(new RecipeCategoryOptionsC2SPacket(RecipeBookCategory.CRAFTING, true, filtering));
        CreativePerkBookClientState.arm();
        this.client.setScreen(new InventoryScreen(this.client.player));
    }
}
