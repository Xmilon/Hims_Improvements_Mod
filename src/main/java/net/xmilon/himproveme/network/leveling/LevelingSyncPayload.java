package net.xmilon.himproveme.network.leveling;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record LevelingSyncPayload(NbtCompound data) implements CustomPayload {
    public static final Id<LevelingSyncPayload> ID = new Id<>(Identifier.of(HimProveMe.MOD_ID, "leveling_sync"));
    public static final PacketCodec<RegistryByteBuf, LevelingSyncPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeNbt(value.data),
            buf -> new LevelingSyncPayload(buf.readNbt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
