package net.xmilon.himproveme.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.xmilon.himproveme.item.custom.DaggerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerDaggerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract void cancelBlockBreaking();

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void himproveme$preventDaggerBlockAttack(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (himproveme$hasMainHandDagger()) {
            this.cancelBlockBreaking();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void himproveme$preventDaggerBlockBreakingProgress(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (himproveme$hasMainHandDagger()) {
            this.cancelBlockBreaking();
            cir.setReturnValue(false);
        }
    }

    private boolean himproveme$hasMainHandDagger() {
        ClientPlayerEntity player = this.client.player;
        return player != null && DaggerItem.isDagger(player.getMainHandStack());
    }
}
