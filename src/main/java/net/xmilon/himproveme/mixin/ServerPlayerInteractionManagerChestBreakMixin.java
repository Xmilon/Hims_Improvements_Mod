package net.xmilon.himproveme.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.xmilon.himproveme.chest.ChestLocking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerChestBreakMixin {
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void himproveme$denyLockedChestOpen(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!ChestLocking.isChestBlock(state) || !ChestLocking.isLocked(world, pos, state)) {
            return;
        }

        boolean owner = ChestLocking.isOwner(player, world, pos, state);
        boolean masterLocked = ChestLocking.isMasterLocked(world, pos, state);
        boolean master = ChestLocking.isHoldingMasterKey(player);
        if (master || (owner && !masterLocked)) {
            return;
        }

        if (world instanceof ServerWorld serverWorld) {
            ChestLocking.denyOpen(serverWorld, pos, player, ChestLocking.getOwnerName(world, pos, state));
        }
        cir.setReturnValue(ActionResult.FAIL);
    }
}
