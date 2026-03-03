package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.block.ModBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends FabricDynamicRegistryProvider {
    public static final RegistryKey<ConfiguredFeature<?, ?>> UPGRADE_GEM_ORE_KEY = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_ore")
    );
    public static final RegistryKey<ConfiguredFeature<?, ?>> UPGRADE_GEM_DEEPSLATE_ORE_KEY = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_deepslate_ore")
    );
    public static final RegistryKey<PlacedFeature> UPGRADE_GEM_ORE_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_ore")
    );
    public static final RegistryKey<PlacedFeature> UPGRADE_GEM_DEEPSLATE_ORE_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(HimProveMe.MOD_ID, "upgrade_gem_deepslate_ore")
    );

    public ModWorldGenProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        ConfiguredFeature<?, ?> stoneOreConfigured = new ConfiguredFeature<>(
                Feature.ORE,
                new OreFeatureConfig(
                        List.of(OreFeatureConfig.createTarget(
                                new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES),
                                ModBlocks.UPGRADE_GEM_ORE.getDefaultState()
                        )),
                        7,
                        0.0f
                )
        );

        ConfiguredFeature<?, ?> deepslateOreConfigured = new ConfiguredFeature<>(
                Feature.ORE,
                new OreFeatureConfig(
                        List.of(OreFeatureConfig.createTarget(
                                new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                                ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE.getDefaultState()
                        )),
                        8,
                        0.0f
                )
        );

        entries.add(UPGRADE_GEM_ORE_KEY, stoneOreConfigured);
        entries.add(UPGRADE_GEM_DEEPSLATE_ORE_KEY, deepslateOreConfigured);

        entries.add(
                UPGRADE_GEM_ORE_PLACED_KEY,
                new PlacedFeature(
                        RegistryEntry.of(stoneOreConfigured),
                        List.of(
                                CountPlacementModifier.of(9),
                                SquarePlacementModifier.of(),
                                HeightRangePlacementModifier.trapezoid(YOffset.fixed(0), YOffset.fixed(80)),
                                BiomePlacementModifier.of()
                        )
                )
        );

        entries.add(
                UPGRADE_GEM_DEEPSLATE_ORE_PLACED_KEY,
                new PlacedFeature(
                        RegistryEntry.of(deepslateOreConfigured),
                        List.of(
                                CountPlacementModifier.of(12),
                                SquarePlacementModifier.of(),
                                HeightRangePlacementModifier.trapezoid(YOffset.fixed(-64), YOffset.fixed(16)),
                                BiomePlacementModifier.of()
                        )
                )
        );
    }

    @Override
    public String getName() {
        return "HimProveMe Worldgen";
    }
}
