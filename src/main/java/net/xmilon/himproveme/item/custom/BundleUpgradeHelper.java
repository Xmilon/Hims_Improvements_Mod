package net.xmilon.himproveme.item.custom;

import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.Fraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BundleUpgradeHelper {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    public static final int CAPACITY_PER_LEVEL = 64;
    private static final String ROOT_KEY = "himproveme_bundle_upgrade";
    private static final String LEVEL_KEY = "Level";
    private static final Fraction NESTED_BUNDLE_OCCUPANCY = Fraction.getFraction(1, 16);
    private static final ThreadLocal<Integer> ACTIVE_BUNDLE_LEVEL = ThreadLocal.withInitial(() -> MIN_LEVEL);

    private BundleUpgradeHelper() {
    }

    public static boolean isBundle(ItemStack stack) {
        return stack.isOf(Items.BUNDLE);
    }

    public static int getLevel(ItemStack stack) {
        if (!isBundle(stack)) {
            return MIN_LEVEL;
        }

        NbtCompound upgradeData = getUpgradeCompound(stack);
        if (upgradeData == null || !upgradeData.contains(LEVEL_KEY, NbtElement.NUMBER_TYPE)) {
            return MIN_LEVEL;
        }

        return MathHelper.clamp(upgradeData.getInt(LEVEL_KEY), MIN_LEVEL, MAX_LEVEL);
    }

    public static void setLevel(ItemStack stack, int level) {
        if (!isBundle(stack)) {
            return;
        }

        int clampedLevel = MathHelper.clamp(level, MIN_LEVEL, MAX_LEVEL);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, compound -> {
            if (clampedLevel <= MIN_LEVEL) {
                compound.remove(ROOT_KEY);
                return;
            }

            NbtCompound upgradeData = new NbtCompound();
            upgradeData.putInt(LEVEL_KEY, clampedLevel);
            compound.put(ROOT_KEY, upgradeData);
        });
    }

    public static boolean canUpgrade(ItemStack stack) {
        return isBundle(stack) && getLevel(stack) < MAX_LEVEL;
    }

    public static int getCapacity(ItemStack stack) {
        return getLevel(stack) * CAPACITY_PER_LEVEL;
    }

    public static Fraction getCapacityFraction(ItemStack stack) {
        return Fraction.getFraction(getLevel(stack), 1);
    }

    public static Fraction getCapacityFraction(int level) {
        return Fraction.getFraction(MathHelper.clamp(level, MIN_LEVEL, MAX_LEVEL), 1);
    }

    public static float getFillFraction(ItemStack stack) {
        normalizeContents(stack);
        BundleContentsComponent contents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return contents.getOccupancy().divideBy(getCapacityFraction(stack)).floatValue();
    }

    public static int getOccupiedUnits(BundleContentsComponent contents) {
        return MathHelper.multiplyFraction(contents.getOccupancy(), CAPACITY_PER_LEVEL);
    }

    public static Fraction getItemOccupancy(ItemStack stack) {
        BundleContentsComponent nestedContents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (nestedContents != null) {
            return NESTED_BUNDLE_OCCUPANCY.add(nestedContents.getOccupancy());
        }

        List<BeehiveBlockEntity.BeeData> bees = stack.getOrDefault(DataComponentTypes.BEES, List.of());
        return !bees.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, stack.getMaxCount());
    }

    public static int getStoredStackLimit(ItemStack stack) {
        return Math.min(stack.getMaxCount(), 99);
    }

    public static ItemStack createPreviewStack(int level) {
        ItemStack stack = new ItemStack(Items.BUNDLE);
        setLevel(stack, level);
        return stack;
    }

    public static Text getLevelText(ItemStack stack) {
        return Text.translatable("item.himproveme.bundle.level", getLevel(stack));
    }

    public static boolean rotateSelection(ItemStack stack, int direction) {
        if (LockableContainerHelper.isLocked(stack)) {
            return false;
        }

        normalizeContents(stack);
        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (contents == null || contents.size() <= 1 || direction == 0) {
            return false;
        }

        List<ItemStack> rotated = new ArrayList<>();
        contents.iterateCopy().forEach(rotated::add);
        Collections.rotate(rotated, direction > 0 ? 1 : -1);
        stack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(List.copyOf(rotated)));
        return true;
    }

    public static boolean canInsert(ItemStack bundleStack, ItemStack sourceStack) {
        if (!isBundle(bundleStack) || LockableContainerHelper.isLocked(bundleStack) || sourceStack.isEmpty() || bundleStack == sourceStack) {
            return false;
        }

        ItemStack bundleCopy = bundleStack.copy();
        ItemStack sourceCopy = sourceStack.copy();
        return tryInsert(bundleCopy, sourceCopy) > 0;
    }

    public static int tryInsert(ItemStack bundleStack, ItemStack sourceStack) {
        if (!isBundle(bundleStack) || LockableContainerHelper.isLocked(bundleStack) || sourceStack.isEmpty() || bundleStack == sourceStack) {
            return 0;
        }

        normalizeContents(bundleStack);

        BundleContentsComponent contents = bundleStack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(contents);
        int inserted;

        beginBundleInteraction(bundleStack);
        try {
            inserted = builder.add(sourceStack);
        } finally {
            endBundleInteraction();
        }

        if (inserted > 0) {
            bundleStack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
        }

        return inserted;
    }

    public static void beginBundleInteraction(ItemStack stack) {
        normalizeContents(stack);
        ACTIVE_BUNDLE_LEVEL.set(getLevel(stack));
    }

    public static void endBundleInteraction() {
        ACTIVE_BUNDLE_LEVEL.remove();
    }

    public static Fraction getActiveCapacityFraction() {
        return getCapacityFraction(ACTIVE_BUNDLE_LEVEL.get());
    }

    public static boolean normalizeContents(ItemStack stack) {
        if (!isBundle(stack)) {
            return false;
        }

        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return false;
        }

        List<ItemStack> normalized = new ArrayList<>();
        boolean changed = false;

        for (ItemStack storedStack : contents.iterateCopy()) {
            int count = storedStack.getCount();
            int limit = getStoredStackLimit(storedStack);

            if (count <= 0) {
                changed = true;
                continue;
            }

            if (count <= limit) {
                normalized.add(storedStack);
                continue;
            }

            changed = true;

            while (count > 0) {
                int chunk = Math.min(count, limit);
                normalized.add(storedStack.copyWithCount(chunk));
                count -= chunk;
            }
        }

        if (!changed) {
            return false;
        }

        stack.set(
                DataComponentTypes.BUNDLE_CONTENTS,
                normalized.isEmpty() ? BundleContentsComponent.DEFAULT : new BundleContentsComponent(List.copyOf(normalized))
        );
        return true;
    }

    private static NbtCompound getUpgradeCompound(ItemStack stack) {
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound root = customData.getNbt();
        if (!root.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        return root.getCompound(ROOT_KEY);
    }
}
