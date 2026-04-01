package net.xmilon.himproveme.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;
import org.jetbrains.annotations.Nullable;

public final class LockableContainerToggleRecipe extends SpecialCraftingRecipe {
    public LockableContainerToggleRecipe(CraftingRecipeCategory category) {
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
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        MatchData match = findMatch(input);
        if (match != null) {
            remainders.set(match.keyIndex(), match.keyStack().copy());
        }
        return remainders;
    }

    @Override
    public net.minecraft.recipe.RecipeSerializer<?> getSerializer() {
        return ModRecipes.LOCKABLE_CONTAINER_TOGGLE;
    }

    @Nullable
    private static MatchData findMatch(CraftingRecipeInput input) {
        ItemStack keyStack = ItemStack.EMPTY;
        int keyIndex = -1;
        ItemStack targetStack = ItemStack.EMPTY;

        for (int slot = 0; slot < input.getSize(); slot++) {
            ItemStack stack = input.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getCount() != 1) {
                return null;
            }

            if (LockableContainerHelper.isLockKey(stack)) {
                if (!keyStack.isEmpty()) {
                    return null;
                }
                keyStack = stack;
                keyIndex = slot;
                continue;
            }

            if (LockableContainerHelper.isLockable(stack)) {
                if (!targetStack.isEmpty()) {
                    return null;
                }
                targetStack = stack;
                continue;
            }

            return null;
        }

        if (keyIndex < 0 || targetStack.isEmpty()) {
            return null;
        }

        ItemStack result = LockableContainerHelper.createLockingResult(keyStack, targetStack);
        if (result.isEmpty()) {
            return null;
        }

        return new MatchData(keyStack.copy(), keyIndex, result);
    }

    private record MatchData(ItemStack keyStack, int keyIndex, ItemStack result) {
    }
}
