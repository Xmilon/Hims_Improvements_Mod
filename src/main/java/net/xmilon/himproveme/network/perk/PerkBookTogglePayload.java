package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookTogglePayload(int instanceIndex, Identifier perkId) implements CustomPayload {
    public static final Id<PerkBookTogglePayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_toggle"));

    public static final PacketCodec<RegistryByteBuf, PerkBookTogglePayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeVarInt(value.instanceIndex());
                        buf.writeIdentifier(value.perkId());
                    },
                    buf -> new PerkBookTogglePayload(buf.readVarInt(), buf.readIdentifier())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
