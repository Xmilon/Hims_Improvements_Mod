package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record DaggerGripStatePayload(boolean gripping, boolean jumpRequested) implements CustomPayload {
    public static final Id<DaggerGripStatePayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "dagger_grip_state"));
    public static final PacketCodec<RegistryByteBuf, DaggerGripStatePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeBoolean(value.gripping);
                buf.writeBoolean(value.jumpRequested);
            },
            buf -> new DaggerGripStatePayload(buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
