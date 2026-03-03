package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookUpgradePayload(int instanceIndex, Identifier perkId) implements CustomPayload {
    public static final Id<PerkBookUpgradePayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_upgrade"));

    public static final PacketCodec<RegistryByteBuf, PerkBookUpgradePayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeVarInt(value.instanceIndex);
                        buf.writeIdentifier(value.perkId);
                    },
                    buf -> new PerkBookUpgradePayload(buf.readVarInt(), buf.readIdentifier())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
