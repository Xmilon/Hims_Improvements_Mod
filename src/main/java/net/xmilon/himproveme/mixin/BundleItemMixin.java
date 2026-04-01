package net.xmilon.himproveme.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.util.Hand;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.access.BundleContentsLevelAccess;
import net.xmilon.himproveme.item.custom.BundleUpgradeHelper;
import net.xmilon.himproveme.item.custom.ContainerPreviewTooltipData;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Inject(method = "onStackClicked", at = @At("HEAD"), cancellable = true)
    private void himproveme$beginStackClick(ItemStack stack, Slot slot, ClickType clickType, net.minecraft.entity.player.PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (LockableContainerHelper.isLocked(stack)) {
            cir.setReturnValue(false);
            return;
        }
        BundleUpgradeHelper.beginBundleInteraction(stack);
    }

    @Inject(method = "onStackClicked", at = @At("RETURN"))
    private void himproveme$endStackClick(ItemStack stack, Slot slot, ClickType clickType, net.minecraft.entity.player.PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        BundleUpgradeHelper.endBundleInteraction();
    }

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void himproveme$beginInventoryClick(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, net.minecraft.entity.player.PlayerEntity player, net.minecraft.inventory.StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (LockableContainerHelper.isLocked(stack)) {
            cir.setReturnValue(false);
            return;
        }
        BundleUpgradeHelper.beginBundleInteraction(stack);
    }

    @Inject(method = "onClicked", at = @At("RETURN"))
    private void himproveme$endInventoryClick(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, net.minecraft.entity.player.PlayerEntity player, net.minecraft.inventory.StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        BundleUpgradeHelper.endBundleInteraction();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void himproveme$denyLockedBundleUse(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!LockableContainerHelper.isLocked(stack)) {
            return;
        }

        if (world.isClient) {
            cir.setReturnValue(TypedActionResult.success(stack));
            return;
        }

        LockableContainerHelper.denyAccess(user);
        cir.setReturnValue(TypedActionResult.fail(stack));
    }

    @Inject(method = "getAmountFilled", at = @At("HEAD"), cancellable = true)
    private static void himproveme$getScaledFillAmount(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(BundleUpgradeHelper.getFillFraction(stack));
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void himproveme$getScaledItemBarStep(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        float fillFraction = BundleUpgradeHelper.getFillFraction(stack);
        cir.setReturnValue(Math.min(1 + MathHelper.floor(fillFraction * 12.0f), 13));
    }

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void himproveme$getUpgradeableTooltip(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (stack.contains(DataComponentTypes.HIDE_TOOLTIP) || stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        if (LockableContainerHelper.isLocked(stack)) {
            cir.setReturnValue(Optional.of(ContainerPreviewTooltipData.blockedView()));
            return;
        }

        BundleUpgradeHelper.normalizeContents(stack);
        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (contents == null) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        ((BundleContentsLevelAccess) (Object) contents).himproveme$setBundleLevel(BundleUpgradeHelper.getLevel(stack));
        cir.setReturnValue(Optional.of(new BundleTooltipData(contents)));
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void himproveme$appendLevelTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (LockableContainerHelper.isLocked(stack)) {
            ci.cancel();
            return;
        }

        BundleUpgradeHelper.normalizeContents(stack);
        BundleContentsComponent contents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        tooltip.add(BundleUpgradeHelper.getLevelText(stack).copy().formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(
                "item.minecraft.bundle.fullness",
                BundleUpgradeHelper.getOccupiedUnits(contents),
                BundleUpgradeHelper.getCapacity(stack)
        ).formatted(Formatting.GRAY));
        ci.cancel();
    }
}
