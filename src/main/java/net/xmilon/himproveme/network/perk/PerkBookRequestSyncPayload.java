package net.xmilon.himproveme.network.perk;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookRequestSyncPayload() implements CustomPayload {
    public static final PerkBookRequestSyncPayload INSTANCE = new PerkBookRequestSyncPayload();
    public static final Id<PerkBookRequestSyncPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_request_sync"));
    public static final PacketCodec<RegistryByteBuf, PerkBookRequestSyncPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
