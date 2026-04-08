package net.xmilon.himproveme.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.xmilon.himproveme.network.perk.AcrobatJumpPayload;
import net.xmilon.himproveme.perk.ClientPerkBookState;
import net.xmilon.himproveme.perk.PerkAccess;

import java.util.Locale;

public final class AcrobatPerkClientHelper {
    private static final int AIR_JUMP_COOLDOWN_TICKS = 80;
    private static final int AIR_JUMP_ARM_DELAY_TICKS = 4;
    private static boolean lastJumpDown;
    private static boolean releasedJumpSinceTakeoff;
    private static int airborneTicks;
    private static int localCooldownTicks;

    private AcrobatPerkClientHelper() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) {
            lastJumpDown = false;
            releasedJumpSinceTakeoff = false;
            airborneTicks = 0;
            localCooldownTicks = 0;
            return;
        }

        ClientPlayerEntity player = client.player;
        boolean jumpDown = client.options.jumpKey.isPressed();
        tickAirborneState(player, jumpDown);

        if (localCooldownTicks > 0) {
            localCooldownTicks--;
            if (hasAcrobatPerk()) {
                double remainingSeconds = localCooldownTicks / 20.0D;
                player.sendMessage(
                        Text.translatable("ability.himproveme.acrobat.cooldown", String.format(Locale.ROOT, "%.1f", remainingSeconds)),
                        true
                );
            }
        }

        if (jumpDown && !lastJumpDown && canRequestAirJump(player)) {
            float sideways = player.input == null ? 0.0F : player.input.movementSideways;
            float forward = player.input == null ? 0.0F : player.input.movementForward;
            ClientPlayNetworking.send(new AcrobatJumpPayload(sideways, forward));
            localCooldownTicks = AIR_JUMP_COOLDOWN_TICKS;
        }
        lastJumpDown = jumpDown;
    }

    private static boolean canRequestAirJump(ClientPlayerEntity player) {
        return localCooldownTicks <= 0
                && releasedJumpSinceTakeoff
                && airborneTicks >= AIR_JUMP_ARM_DELAY_TICKS
                && hasAcrobatPerk()
                && player.isAlive()
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.isFallFlying()
                && !player.getAbilities().flying
                && !DaggerGripClientHelper.isWallGripping(player);
    }

    private static void tickAirborneState(ClientPlayerEntity player, boolean jumpDown) {
        boolean grounded = player.isOnGround()
                || player.isClimbing()
                || player.isTouchingWater()
                || player.isInLava()
                || player.isFallFlying()
                || player.getAbilities().flying;

        if (grounded) {
            airborneTicks = 0;
            releasedJumpSinceTakeoff = false;
            return;
        }

        airborneTicks++;
        if (!jumpDown && airborneTicks >= 2) {
            releasedJumpSinceTakeoff = true;
        }
    }

    private static boolean hasAcrobatPerk() {
        return ClientPerkBookState.getSnapshot().getSelectedInstance().isEnabled(PerkAccess.ACROBAT);
    }
}
