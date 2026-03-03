package net.xmilon.himproveme.chest;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.item.ModItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChestLocking {
    private static final Identifier FAIL_OPEN_SOUND_ID = Identifier.of(HimProveMe.MOD_ID, "fail_open_0");
    private static final Identifier LOCK_TOGGLE_SOUND_ID = Identifier.of(HimProveMe.MOD_ID, "lock_key");
    private static final String MASTER_LOCK_MARKER = "__MASTER_LOCK__";

    private ChestLocking() {
    }

    public static boolean isChestBlock(BlockState state) {
        return state.getBlock() instanceof ChestBlock;
    }

    public static List<ChestBlockEntity> getConnectedChests(World world, BlockPos pos, BlockState state) {
        List<ChestBlockEntity> result = new ArrayList<>();
        BlockEntity base = world.getBlockEntity(pos);
        if (base instanceof ChestBlockEntity chestBase) {
            result.add(chestBase);
        }

        if (!(state.getBlock() instanceof ChestBlock) || state.get(ChestBlock.CHEST_TYPE) == net.minecraft.block.enums.ChestType.SINGLE) {
            return result;
        }

        BlockPos otherPos = pos.offset(ChestBlock.getFacing(state));
        BlockEntity other = world.getBlockEntity(otherPos);
        if (other instanceof ChestBlockEntity chestOther) {
            result.add(chestOther);
        }
        return result;
    }

    public static boolean isLocked(World world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ChestLockDataAccess lock = (ChestLockDataAccess) chest;
            if (lock.himproveme$isLocked()) {
                return true;
            }
        }
        return false;
    }

    public static UUID getOwnerUuid(World world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ChestLockDataAccess lock = (ChestLockDataAccess) chest;
            if (lock.himproveme$isLocked() && lock.himproveme$getOwnerUuid() != null) {
                return lock.himproveme$getOwnerUuid();
            }
        }
        return null;
    }

    public static String getOwnerName(World world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ChestLockDataAccess lock = (ChestLockDataAccess) chest;
            if (lock.himproveme$isLocked()) {
                String owner = lock.himproveme$getOwnerName();
                if (owner != null && !owner.isBlank()) {
                    if (MASTER_LOCK_MARKER.equals(owner)) {
                        return "Master Key";
                    }
                    return owner;
                }
            }
        }
        return "Unknown";
    }

    public static boolean isOwner(PlayerEntity player, World world, BlockPos pos, BlockState state) {
        UUID owner = getOwnerUuid(world, pos, state);
        return owner != null && owner.equals(player.getUuid());
    }

    public static boolean isMasterLocked(World world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ChestLockDataAccess lock = (ChestLockDataAccess) chest;
            if (lock.himproveme$isLocked() && MASTER_LOCK_MARKER.equals(lock.himproveme$getOwnerName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHoldingMasterKey(PlayerEntity player) {
        return player.getMainHandStack().isOf(ModItem.MASTER_KEY) || player.getOffHandStack().isOf(ModItem.MASTER_KEY);
    }

    public static void lock(ServerWorld world, BlockPos pos, BlockState state, PlayerEntity player) {
        UUID ownerUuid = player.getUuid();
        String ownerName = player.getName().getString();
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ((ChestLockDataAccess) chest).himproveme$setLockState(true, ownerUuid, ownerName);
            chest.markDirty();
        }
        playLockToggleSound(world, pos);
    }

    public static void lockMaster(ServerWorld world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ((ChestLockDataAccess) chest).himproveme$setLockState(true, null, MASTER_LOCK_MARKER);
            chest.markDirty();
        }
        playLockToggleSound(world, pos);
    }

    public static void unlock(ServerWorld world, BlockPos pos, BlockState state) {
        for (ChestBlockEntity chest : getConnectedChests(world, pos, state)) {
            ((ChestLockDataAccess) chest).himproveme$setLockState(false, null, null);
            chest.markDirty();
        }
        playLockToggleSound(world, pos);
    }

    public static void denyOpen(ServerWorld world, BlockPos pos, PlayerEntity player, String ownerName) {
        denyOpen(world, pos, player, ownerName, true);
    }

    public static void denyOpen(ServerWorld world, BlockPos pos, PlayerEntity player, String ownerName, boolean notifyPlayer) {
        if (notifyPlayer) {
            if (!"Master Key".equals(ownerName)) {
                player.sendMessage(Text.translatable("chestlock.himproveme.owner", ownerName), false);
            }
            player.sendMessage(Text.translatable("chestlock.himproveme.denied", ownerName).formatted(Formatting.RED), true);
        }
        world.spawnParticles(
                ParticleTypes.SMOKE,
                pos.getX() + 0.5,
                pos.getY() + 0.7,
                pos.getZ() + 0.5,
                12,
                0.2,
                0.15,
                0.2,
                0.01
        );
        world.playSound(null, pos, SoundEvent.of(FAIL_OPEN_SOUND_ID), SoundCategory.BLOCKS, 0.8f, 1.0f);
    }

    private static void playLockToggleSound(ServerWorld world, BlockPos pos) {
        world.playSound(null, pos, SoundEvent.of(LOCK_TOGGLE_SOUND_ID), SoundCategory.BLOCKS, 0.85f, 1.0f);
    }
}
