package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.item.custom.BlowgunItem;

public final class BlowgunNetworking {
    private BlowgunNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BlowgunFirePayload.ID, BlowgunFirePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BlowgunFirePayload.ID, (payload, context) ->
                context.server().execute(() -> tryFire(context.player(), payload.hand())));
    }

    private static void tryFire(ServerPlayerEntity player, Hand hand) {
        if (!BlowgunItem.fire(player, hand)) {
            return;
        }
    }
}
