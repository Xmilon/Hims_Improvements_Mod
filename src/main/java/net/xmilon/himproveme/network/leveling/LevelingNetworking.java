package net.xmilon.himproveme.network.leveling;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.leveling.LevelingManager;

public final class LevelingNetworking {
    private LevelingNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(LevelingSyncPayload.ID, LevelingSyncPayload.CODEC);
    }

    public static void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new LevelingSyncPayload(LevelingManager.createSyncData(player)));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sync(player);
        }
    }
}
