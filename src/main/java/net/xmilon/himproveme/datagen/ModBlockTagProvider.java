package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.xmilon.himproveme.block.ModBlocks;
import net.xmilon.himproveme.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.UPGRADE_GEM_BLOCK)
                .add(ModBlocks.RAW_UPGRADE_GEM_BLOCK)
                .add(ModBlocks.UPGRADE_GEM_ORE)
                .add(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE)
                .add(ModBlocks.MAGIC_BLOCK)
                .add(ModBlocks.STASIS_CHAMBER_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.UPGRADE_GEM_BLOCK)
                .add(ModBlocks.RAW_UPGRADE_GEM_BLOCK)
                .add(ModBlocks.UPGRADE_GEM_ORE)
                .add(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE);



        getOrCreateTagBuilder(ModTags.Blocks.NEEDS_ENDER_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL);
    }




}
