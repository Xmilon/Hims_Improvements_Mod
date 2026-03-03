package net.xmilon.himproveme.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.BiomeKeys;
import net.xmilon.himproveme.entity.ModEntities;

public final class ModEntitySpawns {
    private ModEntitySpawns() {
    }

    public static void register() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeKeys.CRIMSON_FOREST, BiomeKeys.WARPED_FOREST),
                SpawnGroup.CREATURE,
                ModEntities.DODO,
                60,
                2,
                4
        );
    }
}
