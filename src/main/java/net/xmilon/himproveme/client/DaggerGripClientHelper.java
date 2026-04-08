package net.xmilon.himproveme.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import net.xmilon.himproveme.combat.DaggerGripHelper;
import net.xmilon.himproveme.network.DaggerGripStatePayload;
import net.xmilon.himproveme.network.DaggerGripSyncPayload;

public final class DaggerGripClientHelper {
    private static final double MAX_GRIP_START_DISTANCE = 1.45D;
    private static final double MAX_GRIP_START_DISTANCE_BONUS = 0.75D;
    private static final float MAX_GRIP_YAW_OFFSET = 120.0F;
    private static boolean gripping;
    private static boolean sentGripState;
    private static boolean lastJumpDown;
    private static int gripTicks;
    private static boolean gripYawAnchored;
    private static float gripAnchorYaw;

    private DaggerGripClientHelper() {
    }

    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(DaggerGripSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    boolean wasGripping = gripping;
                    gripping = payload.gripping();
                    if (gripping && !wasGripping && context.client().player != null) {
                        gripAnchorYaw = context.client().player.getYaw();
                        gripYawAnchored = true;
                        context.client().player.swingHand(Hand.MAIN_HAND);
                        DualWieldCombatHelper.startOffhandSwing(context.client().player);
                        if (!context.client().options.getPerspective().isFirstPerson()) {
                            context.client().player.swingHand(Hand.OFF_HAND);
                        }
                    } else if (!gripping) {
                        gripYawAnchored = false;
                    }
                }));
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.getNetworkHandler() == null) {
            reset();
            return;
        }

        if (client.currentScreen != null) {
            sendGripStateIfNeeded(false, false);
            lastJumpDown = client.options.jumpKey.isPressed();
            gripping = false;
            gripTicks = 0;
            return;
        }

        ClientPlayerEntity player = client.player;
        boolean attackKeyPressed = client.options.attackKey.isPressed();
        boolean useKeyPressed = client.options.useKey.isPressed();
        boolean wantsGrip = isGripInputActive(player, client.crosshairTarget, attackKeyPressed, useKeyPressed);
        boolean jumpDown = client.options.jumpKey.isPressed();
        boolean jumpRequested = gripping && wantsGrip && jumpDown && !lastJumpDown;

        sendGripStateIfNeeded(wantsGrip, jumpRequested);
        lastJumpDown = jumpDown;

        if (gripping) {
            applyGripYawClamp(player);
            gripTicks++;
        } else {
            gripYawAnchored = false;
            gripTicks = 0;
        }
    }

    public static boolean isWallGripping(LivingEntity entity) {
        return gripping && MinecraftClient.getInstance().player == entity;
    }

    public static float getScrapeWave(float tickDelta) {
        return MathHelper.sin((gripTicks + tickDelta) * 0.55F);
    }

    public static float getBraceWave(float tickDelta) {
        return 0.5F + 0.5F * MathHelper.sin((gripTicks + tickDelta) * 0.23F + 0.8F);
    }

    public static float getPullWave(float tickDelta) {
        return MathHelper.cos((gripTicks + tickDelta) * 0.37F);
    }

    public static boolean isGripInputActive(
            ClientPlayerEntity player,
            HitResult crosshairTarget,
            boolean attackKeyPressed,
            boolean useKeyPressed
    ) {
        if (gripping) {
            return canMaintainGrip(player, attackKeyPressed, useKeyPressed);
        }

        return canStartGrip(player, crosshairTarget, attackKeyPressed, useKeyPressed);
    }

    public static boolean shouldSuppressAttack(
            ClientPlayerEntity player,
            HitResult crosshairTarget,
            boolean attackKeyPressed,
            boolean useKeyPressed
    ) {
        return (gripping && canMaintainGrip(player, attackKeyPressed, useKeyPressed))
                || canStartGrip(player, crosshairTarget, attackKeyPressed, useKeyPressed);
    }

    private static boolean canStartGrip(
            ClientPlayerEntity player,
            HitResult crosshairTarget,
            boolean attackKeyPressed,
            boolean useKeyPressed
    ) {
        if (player == null || crosshairTarget == null) {
            return false;
        }

        if (!(crosshairTarget instanceof BlockHitResult blockHitResult)
                || !blockHitResult.getSide().getAxis().isHorizontal()) {
            return false;
        }

        return attackKeyPressed
                && useKeyPressed
                && !player.isOnGround()
                && player.getVelocity().y < 0.0D
                && !player.isTouchingWater()
                && !player.isInLava()
                && player.getEyePos().squaredDistanceTo(blockHitResult.getPos())
                <= getEffectiveStartDistance(player) * getEffectiveStartDistance(player)
                && DaggerGripHelper.hasDualGripDaggers(player);
    }

    private static boolean canMaintainGrip(ClientPlayerEntity player, boolean attackKeyPressed, boolean useKeyPressed) {
        return player != null
                && attackKeyPressed
                && useKeyPressed
                && player.isAlive()
                && !player.isOnGround()
                && !player.isTouchingWater()
                && !player.isInLava()
                && DaggerGripHelper.hasDualGripDaggers(player);
    }

    private static void sendGripStateIfNeeded(boolean gripping, boolean jumpRequested) {
        if (sentGripState == gripping && !jumpRequested) {
            return;
        }

        ClientPlayNetworking.send(new DaggerGripStatePayload(gripping, jumpRequested));
        sentGripState = gripping;
    }

    private static void reset() {
        gripping = false;
        sentGripState = false;
        lastJumpDown = false;
        gripTicks = 0;
        gripYawAnchored = false;
    }

    private static void applyGripYawClamp(ClientPlayerEntity player) {
        if (!gripYawAnchored) {
            gripAnchorYaw = player.getYaw();
            gripYawAnchored = true;
        }

        float clampedYaw = gripAnchorYaw + MathHelper.clamp(
                MathHelper.wrapDegrees(player.getYaw() - gripAnchorYaw),
                -MAX_GRIP_YAW_OFFSET,
                MAX_GRIP_YAW_OFFSET
        );
        player.setYaw(clampedYaw);
        player.setHeadYaw(clampedYaw);
        player.setBodyYaw(gripAnchorYaw);
    }

    private static double getEffectiveStartDistance(ClientPlayerEntity player) {
        double fallSpeed = Math.max(0.0D, -player.getVelocity().y);
        return MAX_GRIP_START_DISTANCE + MathHelper.clamp(fallSpeed * 0.30D, 0.0D, MAX_GRIP_START_DISTANCE_BONUS);
    }
}
