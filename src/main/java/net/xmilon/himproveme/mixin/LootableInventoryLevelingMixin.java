package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.leveling.LevelingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableInventory.class)
public interface LootableInventoryLevelingMixin {
    @Inject(method = "generateLoot", at = @At("TAIL"))
    private void himproveme$improveGeneratedLoot(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            LevelingManager.onLootGenerated(serverPlayer, (LootableInventory) this);
        }
    }
}
