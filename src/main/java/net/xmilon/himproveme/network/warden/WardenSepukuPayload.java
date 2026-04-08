package net.xmilon.himproveme.network.warden;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

import java.util.UUID;

/**
 * S2C event packet that starts the client-side sepuku animation for one entity.
 */
public record WardenSepukuPayload(UUID entityUuid, int durationTicks) implements CustomPayload {
    public static final Id<WardenSepukuPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "warden_sepuku"));

    public static final PacketCodec<RegistryByteBuf, WardenSepukuPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeUuid(value.entityUuid);
                        buf.writeVarInt(value.durationTicks);
                    },
                    buf -> new WardenSepukuPayload(buf.readUuid(), buf.readVarInt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
