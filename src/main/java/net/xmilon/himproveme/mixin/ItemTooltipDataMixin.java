package net.xmilon.himproveme.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.xmilon.himproveme.item.custom.ContainerPreviewTooltipData;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public abstract class ItemTooltipDataMixin {
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void himproveme$provideShulkerPreview(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (!LockableContainerHelper.isShulkerBox(stack)) {
            return;
        }

        if (stack.contains(DataComponentTypes.HIDE_TOOLTIP) || stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        if (LockableContainerHelper.isLocked(stack)) {
            cir.setReturnValue(Optional.of(ContainerPreviewTooltipData.blockedView()));
            return;
        }

        cir.setReturnValue(Optional.of(ContainerPreviewTooltipData.forShulker(stack)));
    }
}
