package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record ShieldBashPayload(Hand hand, int entityId) implements CustomPayload {
    public static final int SWING_ONLY_ENTITY_ID = -1;
    public static final Id<ShieldBashPayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "shield_bash"));
    public static final PacketCodec<RegistryByteBuf, ShieldBashPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeBoolean(value.hand == Hand.OFF_HAND);
                buf.writeVarInt(value.entityId);
            },
            buf -> new ShieldBashPayload(buf.readBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND, buf.readVarInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
