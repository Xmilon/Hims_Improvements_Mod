package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenPerkCloseMixin {
    @Shadow
    protected MinecraftClient client;

    @Inject(method = "removed", at = @At("HEAD"))
    private void himproveme$closePerkBookWhenInventoryCloses(CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) {
            return;
        }
        if (client == null || client.player == null) {
            return;
        }

        boolean filtering = client.player.getRecipeBook().isFilteringCraftable(RecipeBookCategory.CRAFTING);
        client.player.getRecipeBook().setGuiOpen(RecipeBookCategory.CRAFTING, false);
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new RecipeCategoryOptionsC2SPacket(RecipeBookCategory.CRAFTING, false, filtering));
        }
    }
}
