package net.xmilon.himproveme.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.item.ModItem;
import org.jetbrains.annotations.Nullable;

public final class LockableContainerHelper {
    private static final Identifier FAIL_OPEN_SOUND_ID = Identifier.of(HimProveMe.MOD_ID, "fail_open_0");
    private static final String MASTER_LOCK_MARKER = "__MASTER_LOCK__";
    private static final int SHULKER_SIZE = 27;

    private LockableContainerHelper() {
    }

    public static boolean isPasswordKey(ItemStack stack) {
        return stack.isOf(ModItem.KEY) && getPassword(stack) != null;
    }

    public static boolean isMasterKey(ItemStack stack) {
        return stack.isOf(ModItem.MASTER_KEY);
    }

    public static boolean isLockKey(ItemStack stack) {
        return isMasterKey(stack) || isPasswordKey(stack);
    }

    public static String getPassword(ItemStack stack) {
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) {
            return null;
        }

        String password = customName.getString().trim();
        return password.isEmpty() ? null : password;
    }

    public static boolean isLockable(ItemStack stack) {
        return BundleUpgradeHelper.isBundle(stack) || isShulkerBox(stack);
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock;
    }

    public static ContainerLock getLock(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LOCK, ContainerLock.EMPTY);
    }

    public static boolean isLocked(ItemStack stack) {
        return !getLock(stack).key().isEmpty();
    }

    public static boolean isMasterLocked(ItemStack stack) {
        return MASTER_LOCK_MARKER.equals(getLock(stack).key());
    }

    public static boolean canUnlock(ItemStack lockedStack, ItemStack keyStack) {
        if (!isLocked(lockedStack) || !isLockKey(keyStack)) {
            return false;
        }

        if (isMasterKey(keyStack)) {
            return true;
        }

        return !isMasterLocked(lockedStack) && getLock(lockedStack).canOpen(keyStack);
    }

    public static boolean canExtractFromShulker(ItemStack shulkerStack, ItemStack cursorStack, int direction) {
        if (!isShulkerBox(shulkerStack) || isLocked(shulkerStack) || direction == 0) {
            return false;
        }

        DefaultedList<ItemStack> stacks = getShulkerContents(shulkerStack);
        return findExtractableSlot(stacks, cursorStack, direction) >= 0;
    }

    @Nullable
    public static ItemStack extractFromShulkerToCursor(ItemStack shulkerStack, ItemStack cursorStack, int direction) {
        if (!isShulkerBox(shulkerStack) || isLocked(shulkerStack) || direction == 0) {
            return null;
        }

        DefaultedList<ItemStack> stacks = getShulkerContents(shulkerStack);
        int slotIndex = findExtractableSlot(stacks, cursorStack, direction);
        if (slotIndex < 0) {
            return null;
        }

        ItemStack storedStack = stacks.get(slotIndex);
        ItemStack newCursorStack;

        if (cursorStack.isEmpty()) {
            newCursorStack = storedStack.copy();
            stacks.set(slotIndex, ItemStack.EMPTY);
        } else {
            int movable = Math.min(cursorStack.getMaxCount() - cursorStack.getCount(), storedStack.getCount());
            if (movable <= 0) {
                return null;
            }

            newCursorStack = cursorStack.copy();
            newCursorStack.increment(movable);
            stacks.set(slotIndex, movable == storedStack.getCount() ? ItemStack.EMPTY : storedStack.copyWithCount(storedStack.getCount() - movable));
        }

        setShulkerContents(shulkerStack, stacks);
        return newCursorStack;
    }

    public static void lock(ItemStack stack, String password) {
        stack.set(DataComponentTypes.LOCK, new ContainerLock(password));
    }

    public static void lockMaster(ItemStack stack) {
        stack.set(DataComponentTypes.LOCK, new ContainerLock(MASTER_LOCK_MARKER));
    }

    public static void unlock(ItemStack stack) {
        stack.remove(DataComponentTypes.LOCK);
    }

    public static ItemStack createLockingResult(ItemStack keyStack, ItemStack targetStack) {
        if (!isLockKey(keyStack) || !isLockable(targetStack) || keyStack.getCount() != 1 || targetStack.getCount() != 1) {
            return ItemStack.EMPTY;
        }

        ItemStack result = targetStack.copy();
        result.setCount(1);

        if (!isLocked(targetStack)) {
            if (isMasterKey(keyStack)) {
                lockMaster(result);
            } else {
                lock(result, getPassword(keyStack));
            }
            return result;
        }

        if (canUnlock(targetStack, keyStack)) {
            unlock(result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    public static Text getLockTooltip() {
        return Text.translatable("itemlock.himproveme.tooltip.locked").formatted(Formatting.RED);
    }

    public static Text getUnlockTooltip() {
        return Text.translatable("itemlock.himproveme.tooltip.unlock_hint").formatted(Formatting.DARK_GRAY);
    }

    public static void denyAccess(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        player.sendMessage(Text.translatable("itemlock.himproveme.denied").formatted(Formatting.RED), true);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvent.of(FAIL_OPEN_SOUND_ID), SoundCategory.PLAYERS, 0.8f, 1.0f);
    }

    private static DefaultedList<ItemStack> getShulkerContents(ItemStack shulkerStack) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(SHULKER_SIZE, ItemStack.EMPTY);
        shulkerStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(stacks);
        return stacks;
    }

    private static void setShulkerContents(ItemStack shulkerStack, DefaultedList<ItemStack> stacks) {
        ContainerComponent contents = ContainerComponent.fromStacks(stacks);
        if (contents.equals(ContainerComponent.DEFAULT)) {
            shulkerStack.remove(DataComponentTypes.CONTAINER);
            return;
        }

        shulkerStack.set(DataComponentTypes.CONTAINER, contents);
    }

    private static int findExtractableSlot(DefaultedList<ItemStack> stacks, ItemStack cursorStack, int direction) {
        int start = direction > 0 ? 0 : stacks.size() - 1;
        int end = direction > 0 ? stacks.size() : -1;
        int step = direction > 0 ? 1 : -1;

        if (cursorStack.isEmpty()) {
            for (int index = start; index != end; index += step) {
                if (!stacks.get(index).isEmpty()) {
                    return index;
                }
            }
            return -1;
        }

        if (!cursorStack.isStackable() || cursorStack.getCount() >= cursorStack.getMaxCount()) {
            return -1;
        }

        for (int index = start; index != end; index += step) {
            ItemStack storedStack = stacks.get(index);
            if (storedStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual(cursorStack, storedStack)) {
                continue;
            }
            return index;
        }

        return -1;
    }
}
