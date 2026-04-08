package net.xmilon.himproveme.perk;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.network.perk.AcrobatJumpPayload;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AcrobatPerkHelper {
    private static final long AIR_JUMP_COOLDOWN_TICKS = 80L;
    private static final float AIR_JUMP_FALL_DISTANCE_REDUCTION = 2.0F;
    private static final double AIR_JUMP_VERTICAL_BOOST = 0.42D;
    private static final double AIR_JUMP_HORIZONTAL_BOOST = 0.28D;
    private static final Map<UUID, Long> NEXT_AIR_JUMP_TICK = new ConcurrentHashMap<>();

    private AcrobatPerkHelper() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(AcrobatJumpPayload.ID, AcrobatJumpPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AcrobatJumpPayload.ID, (payload, context) ->
                context.server().execute(() -> tryAirJump(context.player(), payload.sideways(), payload.forward())));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> NEXT_AIR_JUMP_TICK.remove(handler.player.getUuid()));
    }

    private static void tryAirJump(ServerPlayerEntity player, float sideways, float forward) {
        if (!canUseAirJump(player)) {
            return;
        }

        long now = player.getWorld().getTime();
        long nextAirJumpTick = NEXT_AIR_JUMP_TICK.getOrDefault(player.getUuid(), 0L);
        if (now < nextAirJumpTick) {
            double remainingSeconds = (nextAirJumpTick - now) / 20.0D;
            player.sendMessage(
                    Text.translatable("ability.himproveme.acrobat.cooldown", String.format(Locale.ROOT, "%.1f", remainingSeconds))
                            .formatted(Formatting.YELLOW),
                    true
            );
            return;
        }

        Vec3d boostDirection = resolveBoostDirection(player, sideways, forward);
        Vec3d velocity = player.getVelocity();
        double boostedY = Math.max(AIR_JUMP_VERTICAL_BOOST, velocity.y * 0.2D + AIR_JUMP_VERTICAL_BOOST);
        Vec3d boostedVelocity = new Vec3d(
                velocity.x * 0.55D + boostDirection.x * AIR_JUMP_HORIZONTAL_BOOST,
                boostedY,
                velocity.z * 0.55D + boostDirection.z * AIR_JUMP_HORIZONTAL_BOOST
        );

        player.setVelocity(boostedVelocity);
        player.velocityModified = true;
        player.fallDistance = Math.max(0.0F, player.fallDistance - AIR_JUMP_FALL_DISTANCE_REDUCTION);
        NEXT_AIR_JUMP_TICK.put(player.getUuid(), now + AIR_JUMP_COOLDOWN_TICKS);

        player.getServerWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                0.45F,
                1.35F + player.getRandom().nextFloat() * 0.1F
        );
        player.getServerWorld().spawnParticles(
                ParticleTypes.CLOUD,
                player.getX(),
                player.getBodyY(0.5D),
                player.getZ(),
                8,
                0.18D,
                0.08D,
                0.18D,
                0.03D
        );
    }

    private static boolean canUseAirJump(ServerPlayerEntity player) {
        return PerkAccess.hasAcrobat(player)
                && player.isAlive()
                && !player.isSpectator()
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.isFallFlying()
                && !player.getAbilities().flying
                && !player.hasNoGravity();
    }

    private static Vec3d resolveBoostDirection(ServerPlayerEntity player, float sideways, float forward) {
        double inputLengthSquared = sideways * sideways + forward * forward;
        if (inputLengthSquared > 0.001D) {
            double scale = 1.0D / Math.sqrt(inputLengthSquared);
            double localX = sideways * scale;
            double localZ = forward * scale;
            double yawRadians = Math.toRadians(player.getYaw());
            double sin = Math.sin(yawRadians);
            double cos = Math.cos(yawRadians);
            Vec3d direction = new Vec3d(localX * cos - localZ * sin, 0.0D, localZ * cos + localX * sin);
            return direction.normalize();
        }

        Vec3d lookVector = player.getRotationVec(1.0F);
        Vec3d horizontalLook = new Vec3d(lookVector.x, 0.0D, lookVector.z);
        if (horizontalLook.lengthSquared() <= 0.0001D) {
            float yawRadians = player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
            return new Vec3d(-MathHelper.sin(yawRadians), 0.0D, MathHelper.cos(yawRadians));
        }
        return horizontalLook.normalize();
    }
}
