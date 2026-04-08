package net.xmilon.himproveme.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.xmilon.himproveme.perk.PerkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySilentStepsMixin {
    @Inject(method = "playStepSounds", at = @At("HEAD"), cancellable = true)
    private void himproveme$muteWalkingStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (himproveme$hasMuffledSteps()) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldSpawnSprintingParticles", at = @At("HEAD"), cancellable = true)
    private void himproveme$disableSprintParticles(CallbackInfoReturnable<Boolean> cir) {
        if (himproveme$hasMuffledSteps()) {
            cir.setReturnValue(false);
        }
    }

    private boolean himproveme$hasMuffledSteps() {
        return (Object) this instanceof PlayerEntity player && PerkAccess.hasMuffledSteps(player);
    }
}
