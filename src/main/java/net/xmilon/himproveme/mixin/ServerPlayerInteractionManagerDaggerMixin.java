package net.xmilon.himproveme.mixin;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.xmilon.himproveme.item.custom.DaggerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerDaggerMixin {
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
    private void himproveme$preventDaggerBlockBreakPackets(
            BlockPos pos,
            PlayerActionC2SPacket.Action action,
            Direction direction,
            int worldHeight,
            int sequence,
            CallbackInfo ci
    ) {
        if (himproveme$hasMainHandDagger()) {
            ci.cancel();
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void himproveme$preventDaggerTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (himproveme$hasMainHandDagger()) {
            cir.setReturnValue(false);
        }
    }

    private boolean himproveme$hasMainHandDagger() {
        return DaggerItem.isDagger(this.player.getMainHandStack());
    }
}
