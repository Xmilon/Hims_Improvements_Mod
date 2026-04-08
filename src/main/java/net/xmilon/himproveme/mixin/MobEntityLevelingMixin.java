package net.xmilon.himproveme.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.xmilon.himproveme.leveling.LevelingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityLevelingMixin {
    @Inject(method = "updateEnchantments", at = @At("TAIL"))
    private void himproveme$applyLevelScaling(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        LevelingManager.applyMobScaling((MobEntity) (Object) this, world, localDifficulty);
    }
}
