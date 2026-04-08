package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.combat.DualWieldAttackContext;
import net.xmilon.himproveme.combat.DualWieldCombatHelper;
import net.xmilon.himproveme.compat.SpearBackportCompat;

public final class DualWieldAttackNetworking {
    private DualWieldAttackNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(DualWieldAttackPayload.ID, DualWieldAttackPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DualWieldAttackPayload.ID, (payload, context) ->
                context.server().execute(() -> handleAttack(context.player(), payload.entityId())));
    }

    private static void handleAttack(ServerPlayerEntity player, int entityId) {
        if (!DualWieldCombatHelper.canUseOffhandAttack(player) || DualWieldCombatHelper.isBashAttackLocked(player)) {
            return;
        }

        if (SpearBackportCompat.shouldUseOffhandAttack(player)) {
            if (!DualWieldCombatHelper.isOffhandAttackReady(player)) {
                return;
            }

            SpearBackportCompat.stabWithOffhand(player);
            DualWieldCombatHelper.resetOffhandAttackTicks(player);
            player.swingHand(Hand.OFF_HAND, true);
            return;
        }

        if (entityId == DualWieldAttackPayload.SWING_ONLY_ENTITY_ID) {
            DualWieldCombatHelper.resetOffhandAttackTicks(player);
            player.swingHand(Hand.OFF_HAND, true);
            return;
        }

        Entity target = player.getServerWorld().getEntityById(entityId);
        if (!DualWieldCombatHelper.isValidOffhandTarget(player, target)) {
            return;
        }

        if (!player.getServerWorld().getWorldBorder().contains(target.getBlockPos())
                || !DualWieldCombatHelper.isOffhandTargetInRange(player, target)) {
            return;
        }

        DualWieldCombatHelper.prepareTargetForOffhandAttack(target);
        DualWieldAttackContext.run(player, () -> player.attack(target));
        player.swingHand(Hand.OFF_HAND, true);
    }
}
