package net.xmilon.himproveme.combat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.network.DaggerGripStatePayload;
import net.xmilon.himproveme.network.DaggerGripSyncPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DaggerGripManager {
    private static final int BASE_STOP_TICKS = 14;
    private static final int MAX_EXTRA_STOP_TICKS = 22;
    private static final int MAX_SUPPORT_LOSS_TICKS = 6;
    private static final int DURABILITY_INTERVAL_TICKS = 16;
    private static final int PARTICLE_INTERVAL_TICKS = 4;
    private static final int SOUND_INTERVAL_TICKS = 7;
    private static final int REGRIP_COOLDOWN_TICKS = 8;
    private static final double GRIP_ATTACH_SPEED = 0.11D;
    private static final double LOOK_GRIP_RANGE = 1.45D;
    private static final double SUPPORT_GRIP_RANGE = 1.35D;
    private static final double MAX_FALL_LOOK_RANGE_BONUS = 0.95D;
    private static final double MAX_FALL_SUPPORT_RANGE_BONUS = 0.80D;
    private static final double MAX_FALL_HEIGHT_COMPENSATION = 3.25D;
    private static final double WALL_JUMP_VERTICAL_BOOST = 0.30D;
    private static final double WALL_JUMP_HORIZONTAL_BOOST = 0.24D;

    private static final Map<UUID, GripInputState> INPUT_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<UUID, GripRuntimeState> RUNTIME_BY_PLAYER = new ConcurrentHashMap<>();

    private DaggerGripManager() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(DaggerGripStatePayload.ID, DaggerGripStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DaggerGripSyncPayload.ID, DaggerGripSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DaggerGripStatePayload.ID, (payload, context) ->
                context.server().execute(() -> updateInput(context.player(), payload.gripping(), payload.jumpRequested())));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.player, false));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerId = handler.player.getUuid();
            INPUT_BY_PLAYER.remove(playerId);
            RUNTIME_BY_PLAYER.remove(playerId);
        });

        ServerTickEvents.END_SERVER_TICK.register(DaggerGripManager::tick);
    }

    private static void updateInput(ServerPlayerEntity player, boolean gripping, boolean jumpRequested) {
        GripInputState inputState = INPUT_BY_PLAYER.computeIfAbsent(player.getUuid(), ignored -> new GripInputState());
        inputState.gripping = gripping;
        if (jumpRequested) {
            inputState.jumpRequested = true;
        }
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        GripInputState inputState = INPUT_BY_PLAYER.computeIfAbsent(playerId, ignored -> new GripInputState());
        GripRuntimeState runtimeState = RUNTIME_BY_PLAYER.computeIfAbsent(playerId, ignored -> new GripRuntimeState());
        boolean wasGripping = runtimeState.gripping;

        if (runtimeState.regripCooldownTicks > 0) {
            runtimeState.regripCooldownTicks--;
        }

        boolean keepTrying = inputState.gripping && runtimeState.regripCooldownTicks <= 0 && canMaintainGrip(player);
        boolean canLatch = keepTrying && (runtimeState.gripping || canStartGrip(player));
        Direction wallDirection = canLatch ? findGripWall(player, runtimeState.gripping ? runtimeState.wallDirection : null) : null;
        if (wallDirection == null && runtimeState.gripping && runtimeState.wallDirection != null
                && runtimeState.supportLossTicks < MAX_SUPPORT_LOSS_TICKS) {
            runtimeState.supportLossTicks++;
            wallDirection = runtimeState.wallDirection;
        } else if (wallDirection != null) {
            runtimeState.supportLossTicks = 0;
        }
        boolean justWallJumped = false;

        if (runtimeState.gripping && inputState.jumpRequested) {
            performWallJump(player, runtimeState);
            justWallJumped = true;
        }

        inputState.jumpRequested = false;

        if (!justWallJumped && wallDirection != null) {
            applyGrip(player, runtimeState, wallDirection);
        } else {
            clearGrip(player, runtimeState);
        }

        if (wasGripping != runtimeState.gripping) {
            sync(player, runtimeState.gripping);
        }
    }

    private static boolean canStartGrip(ServerPlayerEntity player) {
        return canMaintainGrip(player) && player.getVelocity().y < -0.02D;
    }

    private static boolean canMaintainGrip(ServerPlayerEntity player) {
        return player.isAlive()
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isFallFlying()
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.getAbilities().flying
                && DaggerGripHelper.hasDualGripDaggers(player);
    }

    @Nullable
    private static Direction findGripWall(ServerPlayerEntity player, @Nullable Direction preferredDirection) {
        if (preferredDirection != null && hasGripSupport(player, preferredDirection)) {
            return preferredDirection;
        }

        Direction lookedAtDirection = findLookedAtGripWall(player);
        if (lookedAtDirection != null) {
            return lookedAtDirection;
        }

        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal() && hasGripSupport(player, direction)) {
                return direction;
            }
        }

        return null;
    }

    @Nullable
    private static Direction findLookedAtGripWall(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVector = player.getRotationVec(1.0F);
        double effectiveLookRange = getEffectiveLookGripRange(player);
        double heightCompensation = getFallHeightCompensation(player);
        double[] startOffsets = new double[]{0.0D, heightCompensation * 0.5D, heightCompensation};

        for (double startOffset : startOffsets) {
            Vec3d start = eyePos.add(0.0D, startOffset, 0.0D);
            Vec3d end = start.add(lookVector.multiply(effectiveLookRange));
            BlockHitResult hitResult = world.raycast(new RaycastContext(
                    start,
                    end,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));

            if (hitResult.getType() != HitResult.Type.BLOCK || !hitResult.getSide().getAxis().isHorizontal()) {
                continue;
            }

            if (isGripSurface(world, hitResult.getBlockPos())) {
                return hitResult.getSide().getOpposite();
            }
        }

        return null;
    }

    private static boolean hasGripSupport(ServerPlayerEntity player, Direction direction) {
        ServerWorld world = player.getServerWorld();
        Vec3d directionVector = Vec3d.of(direction.getVector()).multiply(getEffectiveSupportGripRange(player));
        double heightCompensation = getFallHeightCompensation(player);
        double[] sampleHeights = new double[]{
                player.getY() + 0.2D,
                player.getY() + player.getHeight() * 0.55D,
                player.getEyeY() - 0.15D,
                player.getY() + 0.2D + heightCompensation * 0.45D,
                player.getEyeY() - 0.15D + heightCompensation
        };

        for (double sampleY : sampleHeights) {
            Vec3d start = new Vec3d(player.getX(), sampleY, player.getZ());
            Vec3d end = start.add(directionVector);
            BlockHitResult hitResult = world.raycast(new RaycastContext(
                    start,
                    end,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK && isGripSurface(world, hitResult.getBlockPos())) {
                return true;
            }
        }

        return false;
    }

    private static double getEffectiveLookGripRange(ServerPlayerEntity player) {
        return LOOK_GRIP_RANGE + MathHelper.clamp(getCurrentFallSpeed(player) * 0.45D, 0.0D, MAX_FALL_LOOK_RANGE_BONUS);
    }

    private static double getEffectiveSupportGripRange(ServerPlayerEntity player) {
        return SUPPORT_GRIP_RANGE + MathHelper.clamp(getCurrentFallSpeed(player) * 0.35D, 0.0D, MAX_FALL_SUPPORT_RANGE_BONUS);
    }

    private static double getFallHeightCompensation(ServerPlayerEntity player) {
        return MathHelper.clamp(getCurrentFallSpeed(player) * 1.35D, 0.0D, MAX_FALL_HEIGHT_COMPENSATION);
    }

    private static double getCurrentFallSpeed(ServerPlayerEntity player) {
        return Math.max(0.0D, -player.getVelocity().y);
    }

    private static boolean isGripSurface(ServerWorld world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        return !blockState.isAir() && !blockState.getCollisionShape(world, blockPos).isEmpty();
    }

    private static void applyGrip(ServerPlayerEntity player, GripRuntimeState runtimeState, Direction wallDirection) {
        if (!runtimeState.gripping) {
            runtimeState.initialFallSpeed = Math.max(0.0D, -player.getVelocity().y);
            runtimeState.stopTicksTarget = BASE_STOP_TICKS
                    + MathHelper.floor(Math.min(MAX_EXTRA_STOP_TICKS, runtimeState.initialFallSpeed * 12.0D));
        }

        runtimeState.gripping = true;
        runtimeState.wallDirection = wallDirection;
        runtimeState.effectTicks++;
        runtimeState.gravityDisabled = true;

        Vec3d velocity = player.getVelocity();
        int stopTicksTarget = Math.max(BASE_STOP_TICKS, runtimeState.stopTicksTarget);
        double startingDescent = MathHelper.clamp(runtimeState.initialFallSpeed * 0.55D, 0.10D, 0.55D);
        double slowdownProgress = Math.min(1.0D, runtimeState.effectTicks / (double) stopTicksTarget);
        double dampedY = velocity.y * MathHelper.lerp(slowdownProgress, 0.68D, 0.12D);
        double targetDescent = MathHelper.lerp(slowdownProgress, -startingDescent, 0.0D);
        double newY = Math.max(targetDescent, dampedY);
        if (slowdownProgress >= 0.96D || Math.abs(newY) < 0.015D) {
            newY = 0.0D;
        }

        double newX = velocity.x * 0.18D + wallDirection.getOffsetX() * GRIP_ATTACH_SPEED;
        double newZ = velocity.z * 0.18D + wallDirection.getOffsetZ() * GRIP_ATTACH_SPEED;
        if (wallDirection.getAxis() == Direction.Axis.X) {
            newZ *= 0.25D;
        } else {
            newX *= 0.25D;
        }

        player.setNoGravity(true);
        player.setVelocity(newX, newY, newZ);
        player.velocityModified = true;
        player.setSprinting(false);
        player.fallDistance = 0.0F;

        if (runtimeState.effectTicks % DURABILITY_INTERVAL_TICKS == 0) {
            damageGripDaggers(player);
        }
        if (runtimeState.effectTicks % PARTICLE_INTERVAL_TICKS == 0) {
            spawnGripParticles(player, wallDirection);
        }
        if (runtimeState.effectTicks % SOUND_INTERVAL_TICKS == 0) {
            player.getServerWorld().playSound(
                    null,
                    player.getX(),
                    player.getBodyY(0.5D),
                    player.getZ(),
                    SoundEvents.BLOCK_GRINDSTONE_USE,
                    SoundCategory.PLAYERS,
                    0.16F,
                    1.6F + player.getRandom().nextFloat() * 0.1F
            );
        }
    }

    private static void performWallJump(ServerPlayerEntity player, GripRuntimeState runtimeState) {
        Direction wallDirection = runtimeState.wallDirection;
        if (wallDirection == null) {
            clearGrip(player, runtimeState);
            return;
        }

        Vec3d velocity = player.getVelocity();
        player.setNoGravity(false);
        player.setVelocity(
                -wallDirection.getOffsetX() * WALL_JUMP_HORIZONTAL_BOOST + velocity.x * 0.15D,
                WALL_JUMP_VERTICAL_BOOST,
                -wallDirection.getOffsetZ() * WALL_JUMP_HORIZONTAL_BOOST + velocity.z * 0.15D
        );
        player.velocityModified = true;
        player.fallDistance = 0.0F;
        runtimeState.regripCooldownTicks = REGRIP_COOLDOWN_TICKS;

        player.getServerWorld().playSound(
                null,
                player.getX(),
                player.getBodyY(0.5D),
                player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                0.22F,
                1.2F + player.getRandom().nextFloat() * 0.1F
        );
        clearGrip(player, runtimeState);
    }

    private static void clearGrip(ServerPlayerEntity player, GripRuntimeState runtimeState) {
        if (runtimeState.gravityDisabled) {
            player.setNoGravity(false);
            runtimeState.gravityDisabled = false;
        }
        runtimeState.gripping = false;
        runtimeState.effectTicks = 0;
        runtimeState.wallDirection = null;
        runtimeState.supportLossTicks = 0;
        runtimeState.initialFallSpeed = 0.0D;
        runtimeState.stopTicksTarget = 0;
    }

    private static void damageGripDaggers(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        if (!player.getMainHandStack().isEmpty()) {
            player.getMainHandStack().damage(1, world, player,
                    item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));
        }
        if (!player.getOffHandStack().isEmpty()) {
            player.getOffHandStack().damage(1, world, player,
                    item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.OFFHAND));
        }
    }

    private static void spawnGripParticles(ServerPlayerEntity player, Direction wallDirection) {
        ServerWorld world = player.getServerWorld();
        Vec3d center = player.getPos()
                .add(0.0D, player.getHeight() * 0.6D, 0.0D)
                .add(wallDirection.getOffsetX() * 0.35D, 0.0D, wallDirection.getOffsetZ() * 0.35D);
        BlockState wallState = world.getBlockState(player.getBlockPos().offset(wallDirection));

        if (!wallState.isAir()) {
            world.spawnParticles(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, wallState),
                    center.x,
                    center.y,
                    center.z,
                    5,
                    0.08D,
                    0.18D,
                    0.08D,
                    0.01D
            );
        }

        world.spawnParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 1, 0.02D, 0.08D, 0.02D, 0.005D);
    }

    private static void sync(ServerPlayerEntity player, boolean gripping) {
        ServerPlayNetworking.send(player, new DaggerGripSyncPayload(gripping));
    }

    private static final class GripInputState {
        private boolean gripping;
        private boolean jumpRequested;
    }

    private static final class GripRuntimeState {
        private boolean gripping;
        private boolean gravityDisabled;
        private Direction wallDirection;
        private int effectTicks;
        private int regripCooldownTicks;
        private int supportLossTicks;
        private int stopTicksTarget;
        private double initialFallSpeed;
    }
}
