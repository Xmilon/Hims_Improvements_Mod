package net.xmilon.himproveme.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public final class ModEnchantments {
    public static final RegistryKey<Enchantment> BASHING = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(HimProveMe.MOD_ID, "bashing")
    );
    public static final RegistryKey<Enchantment> GRIP = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(HimProveMe.MOD_ID, "grip")
    );
    public static final RegistryKey<Enchantment> FAST_BLOW = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(HimProveMe.MOD_ID, "fast_blow")
    );
    public static final RegistryKey<Enchantment> EXPLOSIVE_BLOW = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(HimProveMe.MOD_ID, "explosive_blow")
    );
    public static final RegistryKey<Enchantment> SELF_SEPUKU = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(HimProveMe.MOD_ID, "self_sepuku")
    );

    private ModEnchantments() {
    }
}
