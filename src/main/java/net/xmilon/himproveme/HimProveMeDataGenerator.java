package net.xmilon.himproveme;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.xmilon.himproveme.datagen.ModBlockTagProvider;
import net.xmilon.himproveme.datagen.ModEntityLootTableProvider;
import net.xmilon.himproveme.datagen.ModItemTagProvider;
import net.xmilon.himproveme.datagen.ModLootTableProvider;
import net.xmilon.himproveme.datagen.ModModelProvider;
import net.xmilon.himproveme.datagen.ModRecipeProvider;
import net.xmilon.himproveme.datagen.ModWorldGenProvider;

public class HimProveMeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModEntityLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModItemTagProvider::new);
		pack.addProvider(ModWorldGenProvider::new);
		pack.addProvider(ModModelProvider::new);
	}
}
