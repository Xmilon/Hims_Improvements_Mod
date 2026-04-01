package net.xmilon.himproveme.mixin;

import net.minecraft.item.ItemStack;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.component.type.BundleContentsComponent$Builder")
public abstract class BundleContentsBuilderRemoveMixin {
    @Shadow
    private List<ItemStack> stacks;

    @Shadow
    private Fraction occupancy;

    @Inject(method = "removeFirst", at = @At("HEAD"), cancellable = true)
    private void himproveme$removeOnlyOneNormalStack(CallbackInfoReturnable<@Nullable ItemStack> cir) {
        if (this.stacks.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        ItemStack firstStack = this.stacks.get(0);
        int extractedCount = Math.min(firstStack.getCount(), firstStack.getMaxCount());
        ItemStack extracted = firstStack.copyWithCount(extractedCount);
        this.occupancy = this.occupancy.subtract(BundleUpgradeHelper.getItemOccupancy(firstStack).multiplyBy(Fraction.getFraction(extractedCount, 1)));

        if (firstStack.getCount() == extractedCount) {
            this.stacks.remove(0);
        } else {
            this.stacks.set(0, firstStack.copyWithCount(firstStack.getCount() - extractedCount));
        }

        cir.setReturnValue(extracted);
    }
}
