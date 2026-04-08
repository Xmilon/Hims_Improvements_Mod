package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.world.ancientcity.AncientCityLootCapture;
import net.xmilon.himproveme.world.ancientcity.AncientCityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the original loot table before vanilla clears it so Ancient City post-processing still knows what generated the chest.
 */
@Mixin(LootableInventory.class)
public interface LootableInventoryAncientCityMixin {
    /**
     * Stores the current loot table key for the duration of the generation call.
     */
    @Inject(method = "generateLoot", at = @At("HEAD"))
    private void himproveme$captureLootTable(PlayerEntity player, CallbackInfo ci) {
        AncientCityLootCapture.set(((LootableInventory) this).getLootTable());
    }

    /**
     * Applies Ancient City chest mutation after vanilla has populated the inventory.
     */
    @Inject(method = "generateLoot", at = @At("TAIL"))
    private void himproveme$mutateAncientCityLoot(PlayerEntity player, CallbackInfo ci) {
        RegistryKey<LootTable> lootTableKey = AncientCityLootCapture.take();

        if (player instanceof ServerPlayerEntity serverPlayer && lootTableKey != null) {
            AncientCityManager.onLootGenerated(serverPlayer, (LootableInventory) this, lootTableKey);
        }
    }
}
