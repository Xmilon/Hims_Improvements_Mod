package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.xmilon.himproveme.item.custom.EnderBundleSoundTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.screen.GenericContainerScreenHandler.class)
public abstract class GenericContainerScreenHandlerMixin {

    @Inject(method = "onClosed", at = @At("HEAD"))
    private void himproveme$playEnderBundleClose(PlayerEntity player, CallbackInfo ci) {
        var world = player.getWorld();
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        EnderBundleSoundTracker.consumePendingPitch(serverPlayer).ifPresent(pitch ->
                world.playSound(null, player.getBlockPos(),
                        SoundEvents.BLOCK_ENDER_CHEST_CLOSE,
                        SoundCategory.PLAYERS,
                        1f,
                        pitch)
        );
    }
}
