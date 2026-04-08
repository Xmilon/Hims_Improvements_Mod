package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record AcrobatJumpPayload(float sideways, float forward) implements CustomPayload {
    public static final Id<AcrobatJumpPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "acrobat_jump"));
    public static final PacketCodec<RegistryByteBuf, AcrobatJumpPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeFloat(value.sideways);
                buf.writeFloat(value.forward);
            },
            buf -> new AcrobatJumpPayload(buf.readFloat(), buf.readFloat())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
