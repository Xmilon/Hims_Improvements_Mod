package net.xmilon.himproveme.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.item.custom.LockableContainerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockLockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void himproveme$denyLockedShulkerOpen(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) {
            return;
        }

        if (!(world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity shulkerBox)) {
            return;
        }

        ContainerLock lock = shulkerBox.getComponents().getOrDefault(DataComponentTypes.LOCK, ContainerLock.EMPTY);
        if (lock.key().isEmpty()) {
            return;
        }

        LockableContainerHelper.denyAccess(player);
        cir.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void himproveme$replaceVanillaTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        ci.cancel();
    }
}
