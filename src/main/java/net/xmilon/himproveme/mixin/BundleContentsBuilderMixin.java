package net.xmilon.himproveme.mixin;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BundleContentsComponent.Builder.class)
public abstract class BundleContentsBuilderMixin {
    @Shadow
    private List<ItemStack> stacks;

    @Shadow
    private Fraction occupancy;

    @Inject(method = "add(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void himproveme$storeItemsAsNormalStacks(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isEmpty() || !stack.getItem().canBeNested()) {
            cir.setReturnValue(0);
            return;
        }

        int added = Math.min(stack.getCount(), this.himproveme$getMaxAllowed(stack));
        if (added <= 0) {
            cir.setReturnValue(0);
            return;
        }

        this.occupancy = this.occupancy.add(BundleUpgradeHelper.getItemOccupancy(stack).multiplyBy(Fraction.getFraction(added, 1)));

        int remaining = added;
        List<ItemStack> movedToFront = new ArrayList<>();

        if (stack.isStackable()) {
            for (int i = 0; i < this.stacks.size() && remaining > 0; ) {
                ItemStack existing = this.stacks.get(i);
                int limit = BundleUpgradeHelper.getStoredStackLimit(existing);

                if (!ItemStack.areItemsAndComponentsEqual(existing, stack) || existing.getCount() >= limit) {
                    i++;
                    continue;
                }

                this.stacks.remove(i);
                int fill = Math.min(limit - existing.getCount(), remaining);
                movedToFront.add(existing.copyWithCount(existing.getCount() + fill));
                remaining -= fill;
            }
        }

        int slotLimit = BundleUpgradeHelper.getStoredStackLimit(stack);
        while (remaining > 0) {
            int chunk = Math.min(slotLimit, remaining);
            movedToFront.add(stack.copyWithCount(chunk));
            remaining -= chunk;
        }

        for (int i = movedToFront.size() - 1; i >= 0; i--) {
            this.stacks.add(0, movedToFront.get(i));
        }

        stack.decrement(added);
        cir.setReturnValue(added);
    }

    @Inject(method = "getMaxAllowed", at = @At("HEAD"), cancellable = true)
    private void himproveme$allowExpandedBundleCapacity(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Fraction capacity = BundleUpgradeHelper.getActiveCapacityFraction();
        if (capacity.compareTo(Fraction.ONE) <= 0) {
            return;
        }

        cir.setReturnValue(this.himproveme$getMaxAllowed(stack));
    }

    private int himproveme$getMaxAllowed(ItemStack stack) {
        Fraction remaining = BundleUpgradeHelper.getActiveCapacityFraction().subtract(this.occupancy);
        return Math.max(remaining.divideBy(BundleUpgradeHelper.getItemOccupancy(stack)).intValue(), 0);
    }
}
