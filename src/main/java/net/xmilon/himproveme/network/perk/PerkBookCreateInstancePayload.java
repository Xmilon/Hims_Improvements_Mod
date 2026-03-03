package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookCreateInstancePayload() implements CustomPayload {
    public static final PerkBookCreateInstancePayload INSTANCE = new PerkBookCreateInstancePayload();
    public static final Id<PerkBookCreateInstancePayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_create_instance"));
    public static final PacketCodec<RegistryByteBuf, PerkBookCreateInstancePayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
