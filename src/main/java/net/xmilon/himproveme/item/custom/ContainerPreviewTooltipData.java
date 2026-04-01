package net.xmilon.himproveme.item.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public record ContainerPreviewTooltipData(List<ItemStack> stacks, int columns, int rows, boolean blocked) implements TooltipData {
    public static ContainerPreviewTooltipData blockedView() {
        return new ContainerPreviewTooltipData(List.of(), 0, 0, true);
    }

    public static ContainerPreviewTooltipData forShulker(ItemStack stack) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(stacks);
        return new ContainerPreviewTooltipData(List.copyOf(stacks), 9, 3, false);
    }
}
