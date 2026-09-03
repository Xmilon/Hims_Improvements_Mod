package net.xmilon.himproveme.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.xmilon.himproveme.effect.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityStunnedFleeMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void himproveme$fleeWhenStunned(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (!self.hasStatusEffect(ModStatusEffects.STUNNED)) return;
        if (self.getWorld().isClient()) return;

        self.setTarget(null);
        self.setAttacking(false);
    }
}
