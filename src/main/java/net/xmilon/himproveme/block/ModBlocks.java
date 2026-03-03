package net.xmilon.himproveme.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.block.custom.MagicBlock;
import net.xmilon.himproveme.block.custom.StasisChamberBlock;

public class ModBlocks {

    // - - - - - - - - - - - - - - - - - RAW AND NON RAW GEM BLOCKS - - - - - - - - - - - - - - -
    public static final Block UPGRADE_GEM_BLOCK = registerBlock("upgrade_gem_block",
            new Block(AbstractBlock.Settings.create().strength(4f)
                    .requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block RAW_UPGRADE_GEM_BLOCK = registerBlock("raw_upgrade_gem_block",
            new Block(AbstractBlock.Settings.create().strength(3f)
                    .requiresTool().sounds(BlockSoundGroup.STONE)));
    // - - - - - - - - - - - - - - - - - ORE AND DEEPSLATE ORE OF -> GEM - - - - - - - - - - - - - - -
    public static final Block UPGRADE_GEM_ORE = registerBlock("upgrade_gem_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
                    AbstractBlock.Settings.create().strength(3f).requiresTool()));

    public static final Block UPGRADE_GEM_DEEPSLATE_ORE = registerBlock("upgrade_gem_deepslate_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(3, 6),
                    AbstractBlock.Settings.create().strength(4f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));
    // - - - - - - - - - - - - - - - - - MAGIC BLOCK - - - - - - - - - - - - - - -
    public static final Block MAGIC_BLOCK = registerBlock("magic_block",
            new MagicBlock(AbstractBlock.Settings.create().strength(1f).requiresTool()));
    public static final Block STASIS_CHAMBER_BLOCK = registerBlock("stasis_chamber_block",
            new StasisChamberBlock(AbstractBlock.Settings.create()
                    .strength(1f)
                    .requiresTool()
                    .luminance(state -> 15)));

    // REGISTER AREA!
    // - - - - - - - - - - - - - - - - - RAW AND NON RAW GEM BLOCKS (register) - - - - - - - - - - - - - - -
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(HimProveMe.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block){
        Registry.register(Registries.ITEM, Identifier.of(HimProveMe.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }


    // - - - - - - - - - - - - - - - - - REGISTER MOD BLOCKS INTO BUILDING BLOCKS - - - - - - - - - - - - - - -
    public static void registerModBlocks() {
        HimProveMe.LOGGER.info("Registering Mod Blocks for " + HimProveMe.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.UPGRADE_GEM_BLOCK);
            entries.add(ModBlocks.RAW_UPGRADE_GEM_BLOCK);
            entries.add(ModBlocks.UPGRADE_GEM_ORE);
            entries.add(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE);
            entries.add(ModBlocks.MAGIC_BLOCK);
            entries.add(ModBlocks.STASIS_CHAMBER_BLOCK);
        });
    }
}
