package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.block.ModBlocks;
import net.xmilon.himproveme.item.ModItem;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerSimpleCubeAll(ModBlocks.UPGRADE_GEM_BLOCK);
        generator.registerSimpleCubeAll(ModBlocks.RAW_UPGRADE_GEM_BLOCK);
        generator.registerSimpleCubeAll(ModBlocks.UPGRADE_GEM_ORE);
        generator.registerSimpleCubeAll(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE);

        Identifier magicSide = Identifier.of(HimProveMe.MOD_ID, "block/magic_block_side");
        Identifier magicEnd = Identifier.of(HimProveMe.MOD_ID, "block/magic_block_end");
        Identifier magicModel = Models.CUBE_COLUMN.upload(
                ModBlocks.MAGIC_BLOCK,
                TextureMap.sideEnd(magicSide, magicEnd),
                generator.modelCollector
        );
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(ModBlocks.MAGIC_BLOCK, magicModel));
        generator.registerParentedItemModel(ModBlocks.MAGIC_BLOCK, magicModel);

        Identifier stasisSide = Identifier.of(HimProveMe.MOD_ID, "block/stasis_chamber_block_side");
        Identifier stasisEnd = Identifier.of(HimProveMe.MOD_ID, "block/stasis_chamber_block_end");
        Identifier stasisModel = Models.CUBE_COLUMN.upload(
                ModBlocks.STASIS_CHAMBER_BLOCK,
                TextureMap.sideEnd(stasisSide, stasisEnd),
                generator.modelCollector
        );
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(ModBlocks.STASIS_CHAMBER_BLOCK, stasisModel));
        generator.registerParentedItemModel(ModBlocks.STASIS_CHAMBER_BLOCK, stasisModel);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItem.UPGRADE_GEM, Models.GENERATED);
        itemModelGenerator.register(ModItem.RAW_UPGRADE_GEM, Models.GENERATED);
        itemModelGenerator.register(ModItem.UPGRADE_GEM_DUST, Models.GENERATED);
        itemModelGenerator.register(ModItem.CHISEL, Models.HANDHELD);
        itemModelGenerator.register(ModItem.KEY, Models.HANDHELD);
        itemModelGenerator.register(ModItem.MASTER_KEY, Models.HANDHELD);
        itemModelGenerator.register(ModItem.LIFE_PEARL, Models.GENERATED);
        itemModelGenerator.register(ModItem.ENDER_ESSENCE, Models.GENERATED);
        itemModelGenerator.register(ModItem.DEAD_NETHER_CORE, Models.GENERATED);
        itemModelGenerator.register(ModItem.NETHER_CORE, Models.GENERATED);
        itemModelGenerator.register(ModItem.GODLY_ELYTRA, Models.GENERATED);
        itemModelGenerator.register(ModItem.BREEZE_STAFF, Models.HANDHELD);
        itemModelGenerator.register(ModItem.ENDER_STAFF, Models.HANDHELD);
        itemModelGenerator.register(ModItem.ENDER_BUNDLE, Models.GENERATED);
        itemModelGenerator.register(ModItem.SPECTRAL_BOW, Models.GENERATED);

        itemModelGenerator.register(
                ModItem.DODO_SPAWN_EGG,
                new Model(Optional.of(Identifier.of("item/template_spawn_egg")), Optional.empty())
        );
    }
}
