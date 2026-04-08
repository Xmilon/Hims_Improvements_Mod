package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record BlowgunFirePayload(Hand hand) implements CustomPayload {
    public static final Id<BlowgunFirePayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "blowgun_fire"));
    public static final PacketCodec<RegistryByteBuf, BlowgunFirePayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeEnumConstant(value.hand),
            buf -> new BlowgunFirePayload(buf.readEnumConstant(Hand.class))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
