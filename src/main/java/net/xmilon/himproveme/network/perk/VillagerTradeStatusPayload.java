package net.xmilon.himproveme.network.perk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record VillagerTradeStatusPayload(NbtCompound data) implements CustomPayload {
    public static final Id<VillagerTradeStatusPayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "villager_trade_status"));

    public static final PacketCodec<RegistryByteBuf, VillagerTradeStatusPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeNbt(value.data),
                    buf -> new VillagerTradeStatusPayload(buf.readNbt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
