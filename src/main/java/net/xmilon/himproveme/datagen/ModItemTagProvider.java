package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.item.ModItem;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    private static final TagKey<Item> UPGRADEABLE_IRON_TOOLS = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of(HimProveMe.MOD_ID, "upgradeable_iron_tools")
    );
    private static final TagKey<Item> TRIM_TEMPLATES = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of("minecraft", "trim_templates")
    );

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(TRIM_TEMPLATES)
                .add(ModItem.UPGRADE_GEM)
                .add(ModItem.NETHER_CORE);

        getOrCreateTagBuilder(UPGRADEABLE_IRON_TOOLS)
                .add(Items.IRON_SWORD)
                .add(Items.IRON_PICKAXE)
                .add(Items.IRON_AXE)
                .add(Items.IRON_SHOVEL)
                .add(Items.IRON_HOE)
                .add(Items.AMETHYST_SHARD)
                .add(ModItem.UPGRADE_GEM)
                .add(ModItem.RAW_UPGRADE_GEM);
    }
}
