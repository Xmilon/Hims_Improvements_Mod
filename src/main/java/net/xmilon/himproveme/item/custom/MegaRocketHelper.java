package net.xmilon.himproveme.item.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.item.ModItem;

import java.util.List;

public final class MegaRocketHelper {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;
    private static final String ROOT_KEY = "himproveme_mega_rocket";
    private static final String POWER_KEY = "Power";
    private static final String SELECTED_LEVEL_KEY = "SelectedLevel";

    private MegaRocketHelper() {
    }

    public static boolean isMegaRocket(ItemStack stack) {
        return stack.isOf(ModItem.MEGA_ROCKET);
    }

    public static boolean isFireworkRocket(ItemStack stack) {
        return stack.isOf(Items.FIREWORK_ROCKET);
    }

    public static ItemStack createStack(int totalPower) {
        ItemStack stack = new ItemStack(ModItem.MEGA_ROCKET);
        int clampedPower = Math.max(0, totalPower);
        int selectedLevel = getHighestSelectableLevel(clampedPower);
        writeState(stack, clampedPower, selectedLevel <= 0 ? MIN_LEVEL : selectedLevel);
        return stack;
    }

    public static int getTotalPower(ItemStack stack) {
        if (!isMegaRocket(stack)) {
            return 0;
        }

        NbtCompound data = getMegaRocketCompound(stack);
        if (data == null || !data.contains(POWER_KEY, NbtElement.NUMBER_TYPE)) {
            return 0;
        }

        return Math.max(0, data.getInt(POWER_KEY));
    }

    public static int getSelectedLevel(ItemStack stack) {
        int totalPower = getTotalPower(stack);
        if (totalPower <= 0) {
            return MIN_LEVEL;
        }

        NbtCompound data = getMegaRocketCompound(stack);
        int highestSelectable = getHighestSelectableLevel(totalPower);
        if (data == null || !data.contains(SELECTED_LEVEL_KEY, NbtElement.NUMBER_TYPE)) {
            return highestSelectable;
        }

        return MathHelper.clamp(data.getInt(SELECTED_LEVEL_KEY), MIN_LEVEL, highestSelectable);
    }

    public static int getActiveLevel(ItemStack stack) {
        int totalPower = getTotalPower(stack);
        if (totalPower <= 0) {
            return 0;
        }

        int selectedLevel = getSelectedLevel(stack);
        return totalPower >= getLevelCost(selectedLevel) ? selectedLevel : getHighestSelectableLevel(totalPower);
    }

    public static int getHighestSelectableLevel(ItemStack stack) {
        return getHighestSelectableLevel(getTotalPower(stack));
    }

    public static int getHighestSelectableLevel(int totalPower) {
        if (totalPower <= 0) {
            return 0;
        }

        return Math.min(MAX_LEVEL, totalPower);
    }

    public static int getLevelCost(int level) {
        return MathHelper.clamp(level, MIN_LEVEL, MAX_LEVEL);
    }

    public static void setSelectedLevel(ItemStack stack, int level) {
        if (!isMegaRocket(stack)) {
            return;
        }

        int totalPower = getTotalPower(stack);
        int highestSelectable = getHighestSelectableLevel(totalPower);
        int normalizedLevel = highestSelectable <= 0 ? MIN_LEVEL : MathHelper.clamp(level, MIN_LEVEL, highestSelectable);
        writeState(stack, totalPower, normalizedLevel);
    }

    public static int cycleSelectedLevel(ItemStack stack) {
        if (!isMegaRocket(stack)) {
            return 0;
        }

        int totalPower = getTotalPower(stack);
        int highestSelectable = getHighestSelectableLevel(totalPower);
        if (highestSelectable <= 0) {
            return 0;
        }

        int currentLevel = getSelectedLevel(stack);
        int nextLevel = currentLevel >= highestSelectable ? MIN_LEVEL : currentLevel + 1;
        writeState(stack, totalPower, nextLevel);
        return nextLevel;
    }

    public static int addPower(ItemStack stack, int addedPower) {
        if (!isMegaRocket(stack) || addedPower <= 0) {
            return 0;
        }

        int currentPower = getTotalPower(stack);
        int updatedPower = currentPower + addedPower;
        int nextSelectedLevel;
        if (currentPower <= 0) {
            nextSelectedLevel = getHighestSelectableLevel(updatedPower);
        } else {
            nextSelectedLevel = Math.min(getSelectedLevel(stack), getHighestSelectableLevel(updatedPower));
        }

        writeState(stack, updatedPower, nextSelectedLevel <= 0 ? MIN_LEVEL : nextSelectedLevel);
        return addedPower;
    }

    public static int consumeSelectedPower(ItemStack stack) {
        if (!isMegaRocket(stack)) {
            return 0;
        }

        int totalPower = getTotalPower(stack);
        int activeLevel = getActiveLevel(stack);
        if (activeLevel <= 0) {
            return 0;
        }

        int cost = getLevelCost(activeLevel);
        if (totalPower < cost) {
            return 0;
        }

        int remainingPower = totalPower - cost;
        int nextSelectedLevel = getHighestSelectableLevel(remainingPower);
        writeState(stack, remainingPower, nextSelectedLevel <= 0 ? MIN_LEVEL : nextSelectedLevel);
        return activeLevel;
    }

    public static int getPowerFromRocketStack(ItemStack rocketStack) {
        if (!isFireworkRocket(rocketStack) || rocketStack.isEmpty()) {
            return 0;
        }

        return getPowerPerRocket(rocketStack) * rocketStack.getCount();
    }

    public static int getPowerPerRocket(ItemStack rocketStack) {
        if (!isFireworkRocket(rocketStack)) {
            return 0;
        }

        FireworksComponent fireworks = rocketStack.get(DataComponentTypes.FIREWORKS);
        int flightDuration = fireworks == null ? MIN_LEVEL : fireworks.flightDuration();
        return MathHelper.clamp(flightDuration, MIN_LEVEL, MAX_LEVEL);
    }

    public static ItemStack createLaunchStack(int level) {
        ItemStack rocketStack = new ItemStack(Items.FIREWORK_ROCKET);
        rocketStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(MathHelper.clamp(level, MIN_LEVEL, MAX_LEVEL), List.of()));
        return rocketStack;
    }

    private static void writeState(ItemStack stack, int totalPower, int selectedLevel) {
        if (!isMegaRocket(stack)) {
            return;
        }

        int clampedPower = Math.max(0, totalPower);
        int highestSelectable = getHighestSelectableLevel(clampedPower);
        int normalizedSelected = highestSelectable <= 0 ? MIN_LEVEL : MathHelper.clamp(selectedLevel, MIN_LEVEL, highestSelectable);

        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, compound -> {
            if (clampedPower <= 0 && normalizedSelected == MIN_LEVEL) {
                compound.remove(ROOT_KEY);
                return;
            }

            NbtCompound megaRocketData = new NbtCompound();
            if (clampedPower > 0) {
                megaRocketData.putInt(POWER_KEY, clampedPower);
                megaRocketData.putInt(SELECTED_LEVEL_KEY, normalizedSelected);
            }
            compound.put(ROOT_KEY, megaRocketData);
        });
    }

    private static NbtCompound getMegaRocketCompound(ItemStack stack) {
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound root = customData.getNbt();
        if (!root.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        return root.getCompound(ROOT_KEY);
    }
}
