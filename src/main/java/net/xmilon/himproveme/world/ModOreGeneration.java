package net.xmilon.himproveme.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.xmilon.himproveme.HimProveMe;

public final class ModOreGeneration {
    private static final RegistryKey<PlacedFeature> UPGRADE_GEM_ORE_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_ore")
    );

    private static final RegistryKey<PlacedFeature> UPGRADE_GEM_DEEPSLATE_ORE_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_deepslate_ore")
    );

    private ModOreGeneration() {
    }

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                UPGRADE_GEM_ORE_PLACED_KEY
        );

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                UPGRADE_GEM_DEEPSLATE_ORE_PLACED_KEY
        );
    }
}
