package net.xmilon.himproveme.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.xmilon.himproveme.block.ModBlocks;
import net.xmilon.himproveme.item.ModItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

	public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generate(RecipeExporter exporter) {
		registerGemRecipes(exporter);
		registerCraftingRecipes(exporter);
		registerStaffRecipes(exporter);
		registerBundle(exporter);
	}

	private void registerGemRecipes(RecipeExporter exporter) {
		ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UPGRADE_GEM_BLOCK)
				.pattern("AAA")
				.pattern("AAA")
				.pattern("AAA")
				.input('A', ModItem.UPGRADE_GEM)
				.criterion(hasItem(ModItem.UPGRADE_GEM), conditionsFromItem(ModItem.UPGRADE_GEM))
				.offerTo(exporter);

		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.UPGRADE_GEM, 9)
				.input(ModBlocks.UPGRADE_GEM_BLOCK)
				.criterion(hasItem(ModBlocks.UPGRADE_GEM_BLOCK), conditionsFromItem(ModBlocks.UPGRADE_GEM_BLOCK))
				.offerTo(exporter);

		offerBlasting(exporter, List.of(ModItem.RAW_UPGRADE_GEM), RecipeCategory.MISC, ModItem.UPGRADE_GEM, 0.2f, 100, "upgrade_gem");

		ShapelessRecipeJsonBuilder rawFromDust = ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.RAW_UPGRADE_GEM);
		for (int i = 0; i < 4; i++) {
			rawFromDust.input(ModItem.UPGRADE_GEM_DUST);
		}
		for (int i = 0; i < 4; i++) {
			rawFromDust.input(Items.DIAMOND);
		}
		rawFromDust.criterion(hasItem(ModItem.UPGRADE_GEM_DUST), conditionsFromItem(ModItem.UPGRADE_GEM_DUST))
				.offerTo(exporter);
	}

	private void registerCraftingRecipes(RecipeExporter exporter) {
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.MAGIC_BLOCK)
				.pattern("###")
				.pattern("#C#")
				.pattern("###")
				.input('#', ModBlocks.UPGRADE_GEM_BLOCK)
				.input('C', Items.DIAMOND)
				.criterion(hasItem(ModBlocks.UPGRADE_GEM_BLOCK), conditionsFromItem(ModBlocks.UPGRADE_GEM_BLOCK))
				.offerTo(exporter);

		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.DEAD_NETHER_CORE)
				.pattern("###")
				.pattern("#C#")
				.pattern("###")
				.input('#', ModItem.LIFE_PEARL)
				.input('C', Items.NETHER_STAR)
				.criterion(hasItem(ModItem.LIFE_PEARL), conditionsFromItem(ModItem.LIFE_PEARL))
				.offerTo(exporter);

		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.KEY)
				.pattern(" ##")
				.pattern(" #C")
				.pattern(" # ")
				.input('#', Items.COPPER_INGOT)
				.input('C', Items.ENDER_PEARL)
				.criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
				.offerTo(exporter);

		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.ENDER_STAFF)
				.input(ModItem.BREEZE_STAFF)
				.input(ModItem.ENDER_ESSENCE)
				.criterion(hasItem(ModItem.ENDER_ESSENCE), conditionsFromItem(ModItem.ENDER_ESSENCE))
				.offerTo(exporter);
	}

	private void registerStaffRecipes(RecipeExporter exporter) {
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.BREEZE_STAFF)
				.pattern("  C")
				.pattern(" # ")
				.pattern("#  ")
				.input('C', Items.DIAMOND)
				.input('#', Items.BREEZE_ROD)
				.criterion(hasItem(Items.BREEZE_ROD), conditionsFromItem(Items.BREEZE_ROD))
				.offerTo(exporter);
/*
		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItem.BREEZE_STAFF)
				.input(ModItem.BREEZE_STAFF)
				.criterion(hasItem(ModItem.BREEZE_STAFF), conditionsFromItem(ModItem.BREEZE_STAFF))
				.offerTo(exporter);

 */
	}

	private void registerBundle(RecipeExporter exporter) {
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.BUNDLE)
				.pattern(" C ")
				.pattern("##-")
				.pattern("## ")
				.input('C', Items.CHEST)
				.input('#', Items.LEATHER)
				.input('-', Items.STRING)
				.criterion(hasItem(Items.LEATHER), conditionsFromItem(Items.LEATHER))
				.offerTo(exporter);
	}
}
