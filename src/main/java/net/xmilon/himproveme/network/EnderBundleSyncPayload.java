package net.xmilon.himproveme.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record EnderBundleSyncPayload(NbtCompound data) implements CustomPayload {
    public static final Id<EnderBundleSyncPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "ender_bundle_sync"));

    public static final PacketCodec<RegistryByteBuf, EnderBundleSyncPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeNbt(value.data),
                    buf -> new EnderBundleSyncPayload(buf.readNbt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}