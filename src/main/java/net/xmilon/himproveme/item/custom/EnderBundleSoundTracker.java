package net.xmilon.himproveme.item.custom;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EnderBundleSoundTracker {

    private static final Map<UUID, Float> PENDING_PITCHES = new ConcurrentHashMap<>();

    private EnderBundleSoundTracker() {}

    public static void markPendingClose(ServerPlayerEntity player, float pitch) {
        PENDING_PITCHES.put(player.getUuid(), pitch);
    }

    public static Optional<Float> consumePendingPitch(ServerPlayerEntity player) {
        return Optional.ofNullable(PENDING_PITCHES.remove(player.getUuid()));
    }

    public static void clear(ServerPlayerEntity player) {
        PENDING_PITCHES.remove(player.getUuid());
    }
}
