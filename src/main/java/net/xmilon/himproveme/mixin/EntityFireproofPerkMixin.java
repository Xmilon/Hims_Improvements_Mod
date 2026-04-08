package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.xmilon.himproveme.perk.PerkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityFireproofPerkMixin {
    @ModifyVariable(method = "setFireTicks", at = @At("HEAD"), argsOnly = true)
    private int himproveme$halveAppliedFireTicks(int fireTicks) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof PlayerEntity player) || !PerkAccess.hasFireproof(player)) {
            return fireTicks;
        }

        int currentFireTicks = self.getFireTicks();
        if (fireTicks <= currentFireTicks) {
            return fireTicks;
        }

        return Math.max(currentFireTicks, Math.max(1, (fireTicks + 1) / 2));
    }
}
