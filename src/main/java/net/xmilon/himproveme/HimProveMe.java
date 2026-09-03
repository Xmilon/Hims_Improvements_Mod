package net.xmilon.himproveme;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.xmilon.himproveme.block.ModBlocks;
import net.xmilon.himproveme.command.HimProveMeCommands;
import net.xmilon.himproveme.entity.ModEntities;
import net.xmilon.himproveme.entity.custom.DodoEntity;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.ModItemGroups;
import net.xmilon.himproveme.item.custom.BundleInteractionEvents;
import net.xmilon.himproveme.item.custom.DoubleEnderSpearInteractionEvents;
import net.xmilon.himproveme.item.custom.ShulkerItemInteractionEvents;
import net.xmilon.himproveme.combat.DaggerGripManager;
import net.xmilon.himproveme.network.BundleScrollNetworking;
import net.xmilon.himproveme.network.BlowgunNetworking;
import net.xmilon.himproveme.network.DualWieldAttackNetworking;
import net.xmilon.himproveme.network.EnderBundleNetworking;
import net.xmilon.himproveme.network.GodlyElytraBoostNetworking;
import net.xmilon.himproveme.network.ShieldBashNetworking;
import net.xmilon.himproveme.network.ShulkerScrollNetworking;
import net.xmilon.himproveme.network.SpecialAbilityManager;
import net.xmilon.himproveme.network.leveling.LevelingNetworking;
import net.xmilon.himproveme.network.perk.PerkBookNetworking;
import net.xmilon.himproveme.network.perk.VillagerTradeNetworking;
import net.xmilon.himproveme.leveling.LevelingManager;
import net.xmilon.himproveme.effect.ModStatusEffects;
import net.xmilon.himproveme.perk.PerkFunctions;
import net.xmilon.himproveme.perk.PerkRegistry;
import net.xmilon.himproveme.perk.AcrobatPerkHelper;
import net.xmilon.himproveme.perk.VillagerPerkHelper;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import net.xmilon.himproveme.prone.ProneNetworking;
import net.xmilon.himproveme.recipe.ModRecipes;
import net.xmilon.himproveme.world.ancientcity.AncientCityManager;
import net.xmilon.himproveme.world.ModEntitySpawns;
import net.xmilon.himproveme.world.ModLootTableModifiers;
import net.xmilon.himproveme.world.ModOreGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HimProveMe implements ModInitializer {
	public static final String MOD_ID = "himproveme";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HimProveMeGameRules.register();
		PerkRegistry.registerDefaults();
		PerkFunctions.registerDefaults();
		ModStatusEffects.register();
		HimProveMeCommands.register();
		ModItemGroups.registerItemGroups();
		ModItem.registeredModItem();
		ModBlocks.registerModBlocks();
		BundleInteractionEvents.register();
		DoubleEnderSpearInteractionEvents.register();
		ShulkerItemInteractionEvents.register();
		DaggerGripManager.register();
		AcrobatPerkHelper.register();
		BlowgunNetworking.register();
		GodlyElytraBoostNetworking.registerServerReceiver();
		DualWieldAttackNetworking.register();
		ShieldBashNetworking.register();
		BundleScrollNetworking.register();
		ShulkerScrollNetworking.register();
		SpecialAbilityManager.register();
		LevelingNetworking.register();
		LevelingManager.register();
		PerkBookNetworking.register();
		VillagerTradeNetworking.register();
		VillagerPerkHelper.register();
		WardenPerkHelper.register();
		ProneNetworking.register();
		EnderBundleNetworking.register();
		ModRecipes.register();
		ModLootTableModifiers.register();
		AncientCityManager.register();
		ModEntities.registerModEntities();
		ModEntitySpawns.register();
		ModOreGeneration.register();

		FabricDefaultAttributeRegistry.register(ModEntities.DODO, DodoEntity.createAttributes());
	}
}