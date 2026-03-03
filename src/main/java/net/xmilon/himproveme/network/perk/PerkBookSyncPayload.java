package net.xmilon.himproveme.network.perk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record PerkBookSyncPayload(NbtCompound data) implements CustomPayload {
    public static final Id<PerkBookSyncPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "perk_book_sync"));

    public static final PacketCodec<RegistryByteBuf, PerkBookSyncPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeNbt(value.data),
                    buf -> new PerkBookSyncPayload(buf.readNbt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
