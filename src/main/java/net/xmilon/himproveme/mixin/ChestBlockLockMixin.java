package net.xmilon.himproveme.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.chest.ChestLocking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class ChestBlockLockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleChestLockUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (!ChestLocking.isChestBlock(state)) {
            return;
        }

        boolean locked = ChestLocking.isLocked(world, pos, state);
        boolean masterLocked = locked && ChestLocking.isMasterLocked(world, pos, state);
        boolean owner = locked && ChestLocking.isOwner(player, world, pos, state);
        boolean hasMasterKey = ChestLocking.isHoldingMasterKey(player);

        if (player.isSneaking() && locked && owner && !masterLocked) {
            ChestLocking.unlock(serverWorld, pos, state);
            player.sendMessage(Text.translatable("chestlock.himproveme.unlocked").formatted(Formatting.GREEN), true);
            cir.setReturnValue(ActionResult.CONSUME);
            return;
        }

        if (locked && !hasMasterKey && (!owner || masterLocked)) {
            String ownerName = ChestLocking.getOwnerName(world, pos, state);
            ChestLocking.denyOpen(serverWorld, pos, player, ownerName);
            cir.setReturnValue(ActionResult.CONSUME);
        }
    }
}
