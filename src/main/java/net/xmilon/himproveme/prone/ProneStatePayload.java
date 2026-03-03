package net.xmilon.himproveme.prone;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record ProneStatePayload(boolean prone) implements CustomPayload {
    public static final Id<ProneStatePayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "prone_state"));
    public static final PacketCodec<RegistryByteBuf, ProneStatePayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.prone),
            buf -> new ProneStatePayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
