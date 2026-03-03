package net.xmilon.himproveme.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.block.ModBlocks;

public class ModItemGroups {

    // - - - - - - - - - - - - - - - - - UPGRADES GROUP - - - - - - - - - - - - - - -
    public static final ItemGroup UPGRADES_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(HimProveMe.MOD_ID, "upgrades"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItem.UPGRADE_GEM))
                    .displayName(Text.translatable("itemgroup.himproveme.himproveme_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItem.UPGRADE_GEM);
                        entries.add(ModItem.RAW_UPGRADE_GEM);
                        entries.add(ModItem.UPGRADE_GEM_DUST);
                        entries.add(ModItem.CHISEL);
                        entries.add(ModItem.KEY);
                        entries.add(ModItem.MASTER_KEY);
                        entries.add(ModItem.LIFE_PEARL);
                        entries.add(ModItem.DEAD_NETHER_CORE);
                        entries.add(ModItem.NETHER_CORE);
                        entries.add(ModItem.GODLY_ELYTRA);
                        entries.add(ModItem.SPECTRAL_BOW);
                        entries.add(ModItem.ENDER_BUNDLE);
                        entries.add(Items.BUNDLE);
                        entries.add(ModItem.BREEZE_STAFF);
                        entries.add(ModItem.ENDER_STAFF);
                        entries.add(ModItem.ENDER_ESSENCE);
                        entries.add(ModItem.DODO_SPAWN_EGG);
                        entries.add(ModItem.ENDER_INGOT);
                        entries.add(ModItem.ENDER_SWORD);
                        entries.add(ModItem.ENDER_PICKAXE);
                        entries.add(ModItem.ENDER_AXE);
                        entries.add(ModItem.ENDER_SHOVEL);
                        entries.add(ModItem.ENDER_HOE);

                    }).build());

    // - - - - - - - - - - - - - - - - - BLOCKS GROUP - - - - - - - - - - - - - - -
    public static final ItemGroup HIMPROVEME_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(HimProveMe.MOD_ID, "himproveme_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.UPGRADE_GEM_BLOCK))
                    .displayName(Text.translatable("itemgroup.himproveme.himproveme_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.RAW_UPGRADE_GEM_BLOCK);
                        entries.add(ModBlocks.UPGRADE_GEM_BLOCK);
                        entries.add(ModBlocks.UPGRADE_GEM_ORE);
                        entries.add(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE);
                        entries.add(ModBlocks.MAGIC_BLOCK);


                    }).build());

    // - - - - - - - - - - - - - - - - - REGISTER AREA - - - - - - - - - - - - - - -
    public static void registerItemGroups(){
        HimProveMe.LOGGER.info("Registering Item Groups for " + HimProveMe.MOD_ID);
    }
}
