package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.xmilon.himproveme.block.ModBlocks;
import net.xmilon.himproveme.item.ModItem;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.UPGRADE_GEM_BLOCK);
        addDrop(ModBlocks.RAW_UPGRADE_GEM_BLOCK);
        addDrop(ModBlocks.MAGIC_BLOCK);
        addDrop(ModBlocks.STASIS_CHAMBER_BLOCK);

        addDrop(ModBlocks.UPGRADE_GEM_ORE, oreDrops(ModBlocks.UPGRADE_GEM_ORE, ModItem.UPGRADE_GEM_DUST));
        addDrop(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE, multipleOreDrops(ModBlocks.UPGRADE_GEM_DEEPSLATE_ORE, ModItem.UPGRADE_GEM_DUST, 2f, 5f));
    }

    private LootTable.Builder multipleOreDrops(Block drop, Item item, float minDrops, float maxDrops) {
        RegistryWrapper.Impl<Enchantment> enchantmentRegistry = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, this.applyExplosionDecay(drop, ((LeafEntry.Builder<?>)
                ItemEntry.builder(item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minDrops, maxDrops))))
                .apply(ApplyBonusLootFunction.oreDrops(enchantmentRegistry.getOrThrow(Enchantments.FORTUNE)))));
    }
}
