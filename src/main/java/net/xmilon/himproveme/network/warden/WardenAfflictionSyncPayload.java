package net.xmilon.himproveme.network.warden;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

import java.util.Arrays;
import java.util.Objects;

/**
 * S2C snapshot for the local player's active Warden affliction state.
 */
public record WardenAfflictionSyncPayload(
        boolean active,
        int profileOrdinal,
        float barPercent,
        boolean controlChaos,
        byte[] movementMapping,
        boolean invertForwardAxis,
        boolean invertSidewaysAxis,
        int sepukuTicksRemaining
) implements CustomPayload {
    public static final Id<WardenAfflictionSyncPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "warden_affliction_sync"));

    public static final PacketCodec<RegistryByteBuf, WardenAfflictionSyncPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeBoolean(value.active);
                        buf.writeVarInt(value.profileOrdinal);
                        buf.writeFloat(value.barPercent);
                        buf.writeBoolean(value.controlChaos);
                        buf.writeByteArray(value.movementMapping);
                        buf.writeBoolean(value.invertForwardAxis);
                        buf.writeBoolean(value.invertSidewaysAxis);
                        buf.writeVarInt(value.sepukuTicksRemaining);
                    },
                    buf -> new WardenAfflictionSyncPayload(
                            buf.readBoolean(),
                            buf.readVarInt(),
                            buf.readFloat(),
                            buf.readBoolean(),
                            buf.readByteArray(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readVarInt()
                    )
            );

    /**
     * Creates a compact inactive payload that clears all client-side Warden effect state.
     */
    public static WardenAfflictionSyncPayload inactive() {
        return new WardenAfflictionSyncPayload(false, -1, 0.0F, false, new byte[]{0, 1, 2, 3}, false, false, 0);
    }

    /**
     * Defensively copies the movement mapping so payload snapshots stay immutable.
     */
    public WardenAfflictionSyncPayload {
        movementMapping = Arrays.copyOf(movementMapping, movementMapping.length);
    }

    /**
     * Compares the movement mapping by contents so sync deduplication can treat equivalent snapshots as equal.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof WardenAfflictionSyncPayload other)) {
            return false;
        }
        return this.active == other.active
                && this.profileOrdinal == other.profileOrdinal
                && Float.compare(this.barPercent, other.barPercent) == 0
                && this.controlChaos == other.controlChaos
                && Arrays.equals(this.movementMapping, other.movementMapping)
                && this.invertForwardAxis == other.invertForwardAxis
                && this.invertSidewaysAxis == other.invertSidewaysAxis
                && this.sepukuTicksRemaining == other.sepukuTicksRemaining;
    }

    /**
     * Mirrors the custom equality contract so the payload behaves correctly in maps.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(
                this.active,
                this.profileOrdinal,
                this.barPercent,
                this.controlChaos,
                this.invertForwardAxis,
                this.invertSidewaysAxis,
                this.sepukuTicksRemaining
        );
        result = 31 * result + Arrays.hashCode(this.movementMapping);
        return result;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
