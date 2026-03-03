package net.xmilon.himproveme.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.xmilon.himproveme.chest.ChestLocking;

public class Key extends Item {
    public Key(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayerEntity player)) {
            return ActionResult.PASS;
        }
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }

        var state = world.getBlockState(context.getBlockPos());
        if (!ChestLocking.isChestBlock(state)) {
            return ActionResult.PASS;
        }

        boolean locked = ChestLocking.isLocked(world, context.getBlockPos(), state);
        if (!locked) {
            if (isMasterKey()) {
                ChestLocking.lockMaster(serverWorld, context.getBlockPos(), state);
                return ActionResult.CONSUME;
            }
            ChestLocking.lock(serverWorld, context.getBlockPos(), state, player);
            player.sendMessage(Text.translatable("chestlock.himproveme.locked").formatted(Formatting.YELLOW), true);
            return ActionResult.CONSUME;
        }

        if (isMasterKey()) {
            ChestLocking.unlock(serverWorld, context.getBlockPos(), state);
            return ActionResult.CONSUME;
        }

        if (ChestLocking.isMasterLocked(world, context.getBlockPos(), state)) {
            ChestLocking.denyOpen(serverWorld, context.getBlockPos(), player, ChestLocking.getOwnerName(world, context.getBlockPos(), state), false);
            return ActionResult.CONSUME;
        }

        if (!ChestLocking.isOwner(player, world, context.getBlockPos(), state)) {
            ChestLocking.denyOpen(serverWorld, context.getBlockPos(), player, ChestLocking.getOwnerName(world, context.getBlockPos(), state), false);
            return ActionResult.CONSUME;
        }

        ChestLocking.unlock(serverWorld, context.getBlockPos(), state);
        player.sendMessage(Text.translatable("chestlock.himproveme.unlocked").formatted(Formatting.GREEN), true);
        return ActionResult.CONSUME;
    }

    protected boolean isMasterKey() {
        return false;
    }
}
