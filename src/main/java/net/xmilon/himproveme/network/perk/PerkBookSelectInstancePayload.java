package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookSelectInstancePayload(int index) implements CustomPayload {
    public static final Id<PerkBookSelectInstancePayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_select_instance"));

    public static final PacketCodec<RegistryByteBuf, PerkBookSelectInstancePayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeVarInt(value.index),
                    buf -> new PerkBookSelectInstancePayload(buf.readVarInt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
