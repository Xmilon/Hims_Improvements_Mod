package net.xmilon.himproveme.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class HeldShulkerBoxScreenHandler extends ScreenHandler {
    private static final int INVENTORY_SIZE = 27;

    private final Inventory inventory;
    private final int lockedHotbarSlot;
    private final int lockedSwapButton;
    private final int lockedScreenSlot;

    public HeldShulkerBoxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, int lockedHotbarSlot, int lockedSwapButton) {
        super(ScreenHandlerType.SHULKER_BOX, syncId);
        checkSize(inventory, INVENTORY_SIZE);
        this.inventory = inventory;
        this.lockedHotbarSlot = lockedHotbarSlot;
        this.lockedSwapButton = lockedSwapButton;
        this.lockedScreenSlot = lockedHotbarSlot >= 0 ? 54 + lockedHotbarSlot : -1;
        inventory.onOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new ShulkerBoxSlot(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            Slot slot = column == lockedHotbarSlot
                    ? new LockedSlot(playerInventory, column, 8 + column * 18, 142)
                    : new Slot(playerInventory, column, 8 + column * 18, 142);
            this.addSlot(slot);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == this.lockedScreenSlot || actionType == SlotActionType.SWAP && button == this.lockedSwapButton) {
            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex == this.lockedScreenSlot) {
            return ItemStack.EMPTY;
        }

        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (slotIndex < this.inventory.size()) {
                if (!this.insertItem(stack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
        if (this.inventory instanceof HeldShulkerBoxInventory heldInventory) {
            heldInventory.saveToStack();
        }

        if (!player.getWorld().isClient) {
            player.getWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.BLOCK_SHULKER_BOX_CLOSE,
                    SoundCategory.PLAYERS,
                    0.8f,
                    0.95f + player.getWorld().random.nextFloat() * 0.1f
            );
        }
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean canTakePartial(PlayerEntity player) {
            return false;
        }
    }
}
