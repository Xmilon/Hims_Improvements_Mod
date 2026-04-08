package net.xmilon.himproveme.world.ancientcity;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

/**
 * Holds the transient loot-table capture used during Ancient City chest post-processing.
 * This lives outside the interface mixin because interface mixins cannot declare real fields.
 */
public final class AncientCityLootCapture {
    private static final ThreadLocal<RegistryKey<LootTable>> CURRENT_LOOT_TABLE = new ThreadLocal<>();

    private AncientCityLootCapture() {
    }

    /**
     * Stores the currently generating loot table for the duration of one loot generation call.
     */
    public static void set(RegistryKey<LootTable> lootTableKey) {
        CURRENT_LOOT_TABLE.set(lootTableKey);
    }

    /**
     * Returns and clears the captured loot table so nested generation calls do not leak across inventories.
     */
    public static RegistryKey<LootTable> take() {
        RegistryKey<LootTable> lootTableKey = CURRENT_LOOT_TABLE.get();
        CURRENT_LOOT_TABLE.remove();
        return lootTableKey;
    }
}
