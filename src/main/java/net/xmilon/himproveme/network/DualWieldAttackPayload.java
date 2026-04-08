package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record DualWieldAttackPayload(int entityId) implements CustomPayload {
    public static final int SWING_ONLY_ENTITY_ID = -1;
    public static final Id<DualWieldAttackPayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "dual_wield_attack"));
    public static final PacketCodec<RegistryByteBuf, DualWieldAttackPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeVarInt(value.entityId),
            buf -> new DualWieldAttackPayload(buf.readVarInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
