package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record BundleScrollPayload(int syncId, int slotId, int direction) implements CustomPayload {
    public static final Id<BundleScrollPayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "bundle_scroll"));
    public static final PacketCodec<RegistryByteBuf, BundleScrollPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.syncId);
                buf.writeVarInt(value.slotId);
                buf.writeVarInt(value.direction);
            },
            buf -> new BundleScrollPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
