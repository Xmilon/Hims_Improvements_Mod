package net.xmilon.himproveme.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityShulkerHarvestMixin {
    @Inject(method = "canHarvest", at = @At("HEAD"), cancellable = true)
    private void himproveme$allowShulkerHarvestByHand(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            cir.setReturnValue(true);
        }
    }
}
