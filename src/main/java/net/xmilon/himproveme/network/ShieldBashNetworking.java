package net.xmilon.himproveme.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.xmilon.himproveme.combat.ShieldBashHelper;

public final class ShieldBashNetworking {
    private ShieldBashNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShieldBashPayload.ID, ShieldBashPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShieldBashPayload.ID, (payload, context) ->
                context.server().execute(() -> ShieldBashHelper.performBash(context.player(), payload.hand(), payload.entityId())));
    }
}
