package net.xmilon.himproveme.item.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public final class HeldShulkerBoxInventory extends SimpleInventory {
    private final ItemStack stack;

    public HeldShulkerBoxInventory(ItemStack stack) {
        super(27);
        this.stack = stack;
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.getHeldStacks());
    }

    @Override
    public void markDirty() {
        super.markDirty();
        saveToStack();
    }

    public void saveToStack() {
        stack.remove(DataComponentTypes.CONTAINER_LOOT);
        ContainerComponent contents = ContainerComponent.fromStacks(this.getHeldStacks());
        if (contents.equals(ContainerComponent.DEFAULT)) {
            stack.remove(DataComponentTypes.CONTAINER);
            return;
        }

        stack.set(DataComponentTypes.CONTAINER, contents);
    }
}
