package net.xmilon.himproveme.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Softens incoming Warden damage.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityWardenDamagePenaltyMixin {
    private static final float WARDEN_DAMAGE_MULTIPLIER = 0.72F;

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float himproveme$adjustIncomingDamage(float amount, DamageSource source) {
        if (source.getAttacker() instanceof WardenEntity || source.getSource() instanceof WardenEntity) {
            amount *= WARDEN_DAMAGE_MULTIPLIER;
        }
        return amount;
    }
}
