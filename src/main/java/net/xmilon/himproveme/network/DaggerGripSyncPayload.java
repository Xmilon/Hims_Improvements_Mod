package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record DaggerGripSyncPayload(boolean gripping) implements CustomPayload {
    public static final Id<DaggerGripSyncPayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "dagger_grip_sync"));
    public static final PacketCodec<RegistryByteBuf, DaggerGripSyncPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.gripping),
            buf -> new DaggerGripSyncPayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
