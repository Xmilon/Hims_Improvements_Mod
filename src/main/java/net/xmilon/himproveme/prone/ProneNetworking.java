package net.xmilon.himproveme.prone;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProneNetworking {
    private static final Map<UUID, Boolean> PRONE_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> APPLIED_STATE_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> TOGGLE_LOCK_UNTIL_TICK = new ConcurrentHashMap<>();
    private static final long TOGGLE_LOCK_TICKS = 20L;

    private ProneNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ProneStatePayload.ID, ProneStatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ProneStatePayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    UUID playerId = context.player().getUuid();
                    long now = context.server().getTicks();
                    long lockUntil = TOGGLE_LOCK_UNTIL_TICK.getOrDefault(playerId, 0L);
                    if (now < lockUntil) {
                        return;
                    }

                    boolean nextProne = !PRONE_BY_PLAYER.getOrDefault(playerId, false);
                    PRONE_BY_PLAYER.put(playerId, nextProne);
                    TOGGLE_LOCK_UNTIL_TICK.put(playerId, now + TOGGLE_LOCK_TICKS);
                    if (!nextProne) {
                        context.player().setSwimming(false);
                    }
                })
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PRONE_BY_PLAYER.remove(handler.player.getUuid()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> APPLIED_STATE_BY_PLAYER.remove(handler.player.getUuid()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TOGGLE_LOCK_UNTIL_TICK.remove(handler.player.getUuid()));
        ServerTickEvents.END_SERVER_TICK.register(ProneNetworking::tick);
    }

    public static boolean isProne(UUID playerId) {
        return PRONE_BY_PLAYER.getOrDefault(playerId, false);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean prone = PRONE_BY_PLAYER.getOrDefault(player.getUuid(), false);
            boolean applied = APPLIED_STATE_BY_PLAYER.getOrDefault(player.getUuid(), false);

            if (prone && !applied) {
                // Enter crawl: same forced-pose path used when a trapdoor pushes the player into crawl.
                player.setSwimming(true);
                player.setPose(EntityPose.SWIMMING);
                APPLIED_STATE_BY_PLAYER.put(player.getUuid(), true);
            } else if (!prone && applied) {
                // Exit crawl: force stand-up transition.
                player.setSwimming(false);
                player.setPose(EntityPose.STANDING);
                APPLIED_STATE_BY_PLAYER.put(player.getUuid(), false);
            } else if (prone) {
                // Keep prone continuously active without extra movement limits.
                player.setSwimming(true);
                player.setPose(EntityPose.SWIMMING);
            }

            if (prone) {
                player.setSprinting(false);
            }
        }
    }
}
