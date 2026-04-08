package net.xmilon.himproveme.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.book.RecipeBookCategory;

public final class CreativePerkBookClientState {
    private static boolean active;

    private CreativePerkBookClientState() {
    }

    public static void arm() {
        active = true;
    }

    public static void clear() {
        active = false;
    }

    public static boolean shouldBypassCreativeRedirect() {
        if (!active) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.getRecipeBook().isGuiOpen(RecipeBookCategory.CRAFTING)) {
            active = false;
            return false;
        }

        return true;
    }
}
