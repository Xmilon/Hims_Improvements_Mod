package net.xmilon.himproveme.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.xmilon.himproveme.HimProveMe;
import net.minecraft.text.Text;
import net.xmilon.himproveme.entity.ModEntities;
import net.xmilon.himproveme.item.custom.*;

import java.util.List;

public class ModItem {

    public static Item UPGRADE_GEM = registerItem(
            "upgrade_gem",
            new SmithingTemplateItem(
                    Text.literal("Applies to equipment"),
                    Text.literal("Ingredients"),
                    Text.literal("Upgrade Gem Template"),
                    Text.literal("Insert equipment"),
                    Text.literal("Insert material"),
                    List.of(),
                    List.of()
            )
    );

    public static final Item RAW_UPGRADE_GEM = registerItem("raw_upgrade_gem", new Item(new Item.Settings()));

    public static final Item UPGRADE_GEM_DUST = registerItem("upgrade_gem_dust", new Item(new Item.Settings()));

    public static final Item CHISEL = registerItem("chisel", new ChiselItem(new Item.Settings().maxDamage(512)));

    public static final Item KEY = registerItem("key", new Key(new Item.Settings()));
    public static final Item MASTER_KEY = registerItem("master_key", new MasterKey(new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item LIFE_PEARL = registerItem("life_pearl", new Item(new Item.Settings().rarity(Rarity.RARE)));

    public static final Item ENDER_ESSENCE = registerItem("ender_essence", new Item(new Item.Settings()));

    public static final Item DEAD_NETHER_CORE = registerItem("dead_nether_core", new DeadNetherCoreItem(new Item.Settings().fireproof().rarity(Rarity.EPIC)));

    public static final Item NETHER_CORE = registerItem("nether_core", new NetherCoreItem());

    public static final Item GODLY_ELYTRA = registerItem("godly_elytra",
            new GodlyElytraItem(new Item.Settings()
                    .maxCount(1)
                    .fireproof()
                    .rarity(Rarity.EPIC)));

    public static final Item SPECTRAL_BOW = registerItem("spectral_bow",
            new SpectralBowItem(new Item.Settings()
                    .maxCount(1)
                    .fireproof()
                    .rarity(Rarity.EPIC)));

    public static final Item BREEZE_STAFF = registerItem("breeze_staff",
            new BreezeStaffItem(new Item.Settings()
                    .maxCount(1)
                    .maxDamage(16)
                    .rarity(Rarity.RARE)));

    public static final Item ENDER_BUNDLE = registerItem("ender_bundle", new EnderBundleItem(new Item.Settings().maxCount(1)));
    public static final Item ENDER_STAFF = registerItem("ender_staff",
            new EnderStaffItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)));

    public static final Item DODO_SPAWN_EGG = registerItem("dodo_spawn_egg",
            new SpawnEggItem(ModEntities.DODO, 0x945f0b, 0x56401e, new Item.Settings()));

    public static final Item ENDER_INGOT = registerItem("ender_ingot", new Item(new Item.Settings()));

    public static final Item RAW_ENDER_ESSENCE = registerItem("raw_ender_essence", new Item(new Item.Settings()));

    // - - - - - - - - - - - - - -  ENDER ITEMS

    public static final Item ENDER_SWORD = registerItem("ender_sword",
            new SwordItem(ModToolMaterials.ENDER_INGOT, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.ENDER_INGOT, 5, -2.0f))));

    public static final Item ENDER_PICKAXE = registerItem("ender_pickaxe",
            new PickaxeItem(ModToolMaterials.ENDER_INGOT, new Item.Settings()
                    .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.ENDER_INGOT, 3, -2.8f))));

    public static final Item ENDER_AXE = registerItem("ender_axe",
            new AxeItem(ModToolMaterials.ENDER_INGOT, new Item.Settings()
                    .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.ENDER_INGOT, 10, -3.1f))));

    public static final Item ENDER_SHOVEL = registerItem("ender_shovel",
            new ShovelItem(ModToolMaterials.ENDER_INGOT, new Item.Settings()
                    .attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.ENDER_INGOT, 1, -3.0f))));

    public static final Item ENDER_HOE = registerItem("ender_hoe",
            new HoeItem(ModToolMaterials.ENDER_INGOT, new Item.Settings()
                    .attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterials.ENDER_INGOT, 0, -3.5f))));



    // - - - - - - - - - - - -

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(HimProveMe.MOD_ID, name), item);
    }

    public static void registeredModItem(){
        HimProveMe.LOGGER.info("Registering Mod Items for " + HimProveMe.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(UPGRADE_GEM);
            entries.add(RAW_UPGRADE_GEM);
            entries.add(UPGRADE_GEM_DUST);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(Items.BUNDLE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ENDER_STAFF);
        });
    }
}
