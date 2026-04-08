package net.xmilon.himproveme.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

/**
 * Central registry for the custom Warden-perk status effects.
 */
public final class ModStatusEffects {
    public static final RegistryEntry.Reference<StatusEffect> BLEEDING = register("bleeding_effect_icon", new BleedingStatusEffect());
    public static final RegistryEntry.Reference<StatusEffect> STUNNED = register("stunned_effect_icon", new StunnedStatusEffect());
    public static final RegistryEntry.Reference<StatusEffect> FRENZY = register("frenzy_effect_icon", new FrenzyStatusEffect());

    private ModStatusEffects() {
    }

    /**
     * Forces class initialization so the static registry entries are created during mod bootstrap.
     */
    public static void register() {
        HimProveMe.LOGGER.info("Registering Warden perk status effects for {}", HimProveMe.MOD_ID);
    }

    /**
     * Registers a custom status effect and keeps the registry entry for later effect application/removal.
     */
    private static RegistryEntry.Reference<StatusEffect> register(String path, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(HimProveMe.MOD_ID, path), effect);
    }
}
