package net.xmilon.himproveme.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.combat.BlowgunShotHelper;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import net.xmilon.himproveme.network.BlowgunFirePayload;

public final class BlowgunClientHelper {
    private static int mainHandRetryCooldown;
    private static int offHandRetryCooldown;
    private static int mainHandRecoilTicks;
    private static int offHandRecoilTicks;

    private BlowgunClientHelper() {
    }

    public static void tick(MinecraftClient client) {
        if (mainHandRetryCooldown > 0) {
            mainHandRetryCooldown--;
        }
        if (offHandRetryCooldown > 0) {
            offHandRetryCooldown--;
        }
        if (mainHandRecoilTicks > 0) {
            mainHandRecoilTicks--;
        }
        if (offHandRecoilTicks > 0) {
            offHandRecoilTicks--;
        }

        if (client.player == null || client.currentScreen != null) {
            return;
        }

        Hand activeHand = getActiveBlowgunHand(client.player);
        if (activeHand != null && client.interactionManager != null && client.options.attackKey.isPressed()) {
            client.interactionManager.cancelBlockBreaking();
            tryFireActiveBlowgun(client, activeHand);
        }

        if (!BlowgunItem.isAiming(client.player, Hand.MAIN_HAND)) {
            mainHandRecoilTicks = 0;
        }
        if (!BlowgunItem.isAiming(client.player, Hand.OFF_HAND)) {
            offHandRecoilTicks = 0;
        }
    }

    public static boolean tryHandleAttack(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) {
            return false;
        }

        return getActiveBlowgunHand(client.player) != null;
    }

    public static boolean isAiming(LivingEntity entity, Hand hand) {
        return BlowgunItem.isAiming(entity, hand);
    }

    public static float getAimProgress(LivingEntity entity, Hand hand, float tickDelta) {
        return BlowgunItem.getAimProgress(entity, hand, tickDelta);
    }

    public static float getRecoil(Hand hand, float tickDelta) {
        int recoilTicks = hand == Hand.MAIN_HAND ? mainHandRecoilTicks : offHandRecoilTicks;
        if (recoilTicks <= 0) {
            return 0.0F;
        }

        float normalized = MathHelper.clamp((recoilTicks - tickDelta) / BlowgunItem.RECOIL_TICKS, 0.0F, 1.0F);
        return MathHelper.sin(normalized * MathHelper.PI);
    }

    private static Hand getActiveBlowgunHand(ClientPlayerEntity player) {
        if (BlowgunItem.isAiming(player, Hand.MAIN_HAND)) {
            return Hand.MAIN_HAND;
        }
        if (BlowgunItem.isAiming(player, Hand.OFF_HAND)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private static int getRetryCooldown(Hand hand) {
        return hand == Hand.MAIN_HAND ? mainHandRetryCooldown : offHandRetryCooldown;
    }

    private static void setRetryCooldown(Hand hand, int cooldown) {
        if (hand == Hand.MAIN_HAND) {
            mainHandRetryCooldown = cooldown;
        } else {
            offHandRetryCooldown = cooldown;
        }
    }

    private static void setRecoilTicks(Hand hand, int ticks) {
        if (hand == Hand.MAIN_HAND) {
            mainHandRecoilTicks = ticks;
        } else {
            offHandRecoilTicks = ticks;
        }
    }

    private static void tryFireActiveBlowgun(MinecraftClient client, Hand hand) {
        if (getRetryCooldown(hand) > 0) {
            return;
        }

        ItemStack stack = client.player.getStackInHand(hand);
        if (!BlowgunItem.isBlowgun(stack) || client.player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            return;
        }

        ItemStack ammoStack = BlowgunShotHelper.getAmmo(client.player, stack);
        if (!ammoStack.isEmpty()) {
            ClientPlayNetworking.send(new BlowgunFirePayload(hand));
            client.player.swingHand(hand);
            client.player.getItemCooldownManager().set(stack.getItem(), BlowgunShotHelper.getShotCooldownTicks(client.player, stack, ammoStack));
            setRecoilTicks(hand, BlowgunItem.RECOIL_TICKS);
            return;
        }

        client.player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, 0.18F, 1.3F);
        setRetryCooldown(hand, 6);
    }
}
