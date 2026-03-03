package net.xmilon.himproveme.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public record SpecialAbilityTogglePayload() implements CustomPayload {
    public static final SpecialAbilityTogglePayload INSTANCE = new SpecialAbilityTogglePayload();
    public static final Id<SpecialAbilityTogglePayload> ID =
            new Id<>(Identifier.of(HimProveMe.MOD_ID, "special_ability_toggle"));
    public static final PacketCodec<RegistryByteBuf, SpecialAbilityTogglePayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
