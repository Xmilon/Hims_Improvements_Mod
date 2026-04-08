package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.xmilon.himproveme.client.CreativePerkBookClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenCreativePerkMixin {
    @Redirect(
            method = {"init", "handledScreenTick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasCreativeInventory()Z"
            )
    )
    private boolean himproveme$keepPerkInventoryOpenForCreativePlayers(ClientPlayerInteractionManager interactionManager) {
        return interactionManager.hasCreativeInventory() && !CreativePerkBookClientState.shouldBypassCreativeRedirect();
    }
}
