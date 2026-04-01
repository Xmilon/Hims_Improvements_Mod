package net.xmilon.himproveme.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public final class ModRecipes {
    public static final RecipeSerializer<BundleUpgradeSmithingRecipe> BUNDLE_UPGRADE_SMITHING = Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier.of(HimProveMe.MOD_ID, "bundle_upgrade_smithing"),
            new BundleUpgradeSmithingRecipe.Serializer()
    );
    public static final RecipeSerializer<LockableContainerToggleRecipe> LOCKABLE_CONTAINER_TOGGLE = Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier.of(HimProveMe.MOD_ID, "crafting_special_lockable_container_toggle"),
            new SpecialRecipeSerializer<>(LockableContainerToggleRecipe::new)
    );

    private ModRecipes() {
    }

    public static void register() {
        HimProveMe.LOGGER.info("Registering recipes for {}", HimProveMe.MOD_ID);
    }
}
