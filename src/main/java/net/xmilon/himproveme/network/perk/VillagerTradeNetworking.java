package net.xmilon.himproveme.network.perk;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class VillagerTradeNetworking {
    private VillagerTradeNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(VillagerTradeStatusPayload.ID, VillagerTradeStatusPayload.CODEC);
    }
}
