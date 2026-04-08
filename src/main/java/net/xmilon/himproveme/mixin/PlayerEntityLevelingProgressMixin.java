package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.xmilon.himproveme.leveling.LevelingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityLevelingProgressMixin {
    @Inject(method = "addExperience", at = @At("TAIL"))
    private void himproveme$trackVanillaExperience(int experience, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            LevelingManager.onVanillaExperienceGain(player, experience);
        }
    }

    @Inject(method = "onKilledOther", at = @At("TAIL"))
    private void himproveme$trackKillReward(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerEntity player && cir.getReturnValueZ()) {
            LevelingManager.onKilledEntity(player, other);
        }
    }
}
