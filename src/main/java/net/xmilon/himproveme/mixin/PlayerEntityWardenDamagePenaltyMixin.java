package net.xmilon.himproveme.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Adds the unlocked-Warden-perk drawback and softens incoming Warden hits so the overall difficulty stays tense instead of spiky.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityWardenDamagePenaltyMixin {
    private static final float UNLOCKED_WARDEN_INCOMING_DAMAGE_MULTIPLIER = 1.08F;
    private static final float WARDEN_DAMAGE_MULTIPLIER = 0.72F;

    /**
     * Slightly increases incoming damage after unlocking a Warden perk and globally reduces Warden damage to a saner level.
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float himproveme$adjustIncomingDamage(float amount, DamageSource source) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (WardenPerkHelper.hasAnyUnlockedWardenPerk(self)) {
            amount *= UNLOCKED_WARDEN_INCOMING_DAMAGE_MULTIPLIER;
        }

        if (source.getAttacker() instanceof WardenEntity || source.getSource() instanceof WardenEntity) {
            amount *= WARDEN_DAMAGE_MULTIPLIER;
        }

        return amount;
    }
}
