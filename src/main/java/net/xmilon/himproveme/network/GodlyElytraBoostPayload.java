package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record GodlyElytraBoostPayload() implements CustomPayload {
    public static final GodlyElytraBoostPayload INSTANCE = new GodlyElytraBoostPayload();
    public static final Id<GodlyElytraBoostPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "godly_elytra_boost"));
    public static final PacketCodec<RegistryByteBuf, GodlyElytraBoostPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
