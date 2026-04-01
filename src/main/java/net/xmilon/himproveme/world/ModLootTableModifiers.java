package net.xmilon.himproveme.world;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;

public final class ModLootTableModifiers {
    private static final float MINESHAFT_BUNDLE_CHANCE = 0.15f;
    private static final float DUNGEON_BUNDLE_CHANCE = 0.10f;

    private ModLootTableModifiers() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            if (LootTables.ABANDONED_MINESHAFT_CHEST.equals(key)) {
                tableBuilder.pool(createBundlePool(MINESHAFT_BUNDLE_CHANCE));
            } else if (LootTables.SIMPLE_DUNGEON_CHEST.equals(key)) {
                tableBuilder.pool(createBundlePool(DUNGEON_BUNDLE_CHANCE));
            }
        });
    }

    private static LootPool.Builder createBundlePool(float chance) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0f))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(Items.BUNDLE));
    }
}
