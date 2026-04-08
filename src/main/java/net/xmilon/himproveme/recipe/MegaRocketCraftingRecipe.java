package net.xmilon.himproveme.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.MegaRocketHelper;
import org.jetbrains.annotations.Nullable;

public final class MegaRocketCraftingRecipe extends SpecialCraftingRecipe {
    private static final ItemStack PREVIEW_RESULT = MegaRocketHelper.createStack(2);

    public MegaRocketCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return findMatch(input) != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        MatchData match = findMatch(input);
        return match == null ? ItemStack.EMPTY : match.result().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        return PREVIEW_RESULT.copy();
    }

    @Override
    public net.minecraft.recipe.RecipeSerializer<?> getSerializer() {
        return ModRecipes.MEGA_ROCKET_CRAFTING;
    }

    @Nullable
    private static MatchData findMatch(CraftingRecipeInput input) {
        ItemStack megaRocketStack = ItemStack.EMPTY;
        int totalAddedPower = 0;
        int rocketCount = 0;

        for (int slot = 0; slot < input.getSize(); slot++) {
            ItemStack stack = input.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (MegaRocketHelper.isMegaRocket(stack)) {
                if (!megaRocketStack.isEmpty() || stack.getCount() != 1) {
                    return null;
                }
                megaRocketStack = stack;
                continue;
            }

            if (MegaRocketHelper.isFireworkRocket(stack)) {
                if (stack.getCount() != 1) {
                    return null;
                }

                int addedPower = MegaRocketHelper.getPowerPerRocket(stack);
                if (addedPower <= 0) {
                    return null;
                }
                totalAddedPower += addedPower;
                rocketCount++;
                continue;
            }

            return null;
        }

        if (rocketCount <= 0 || totalAddedPower <= 0) {
            return null;
        }

        if (megaRocketStack.isEmpty()) {
            if (rocketCount < 2) {
                return null;
            }
            return new MatchData(MegaRocketHelper.createStack(totalAddedPower));
        }

        ItemStack result = megaRocketStack.copy();
        MegaRocketHelper.addPower(result, totalAddedPower);
        return new MatchData(result);
    }

    private record MatchData(ItemStack result) {
    }
}
