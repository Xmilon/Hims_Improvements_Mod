package net.xmilon.himproveme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.HungerManager;
import net.xmilon.himproveme.effect.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Slows natural regeneration while bleeding by stretching the vanilla hunger-based healing timers.
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerBleedingMixin {
    /**
     * Reduces the fast saturation-heal branch to roughly twenty percent of vanilla when the player is bleeding.
     */
    @ModifyConstant(method = "update", constant = @Constant(intValue = 10), require = 0)
    private int himproveme$slowFastBleedingRegen(int original, PlayerEntity player) {
        return himproveme$isBleeding(player) ? original * 5 : original;
    }

    /**
     * Reduces the normal natural-regeneration branch to roughly twenty percent of vanilla when the player is bleeding.
     */
    @ModifyConstant(method = "update", constant = @Constant(intValue = 80, ordinal = 0), require = 0)
    private int himproveme$slowNormalBleedingRegen(int original, PlayerEntity player) {
        return himproveme$isBleeding(player) ? original * 5 : original;
    }

    /**
     * Uses the marker status effect so the regen slowdown stays tied to the same server-side affliction state.
     */
    private boolean himproveme$isBleeding(PlayerEntity player) {
        return player.hasStatusEffect(ModStatusEffects.BLEEDING);
    }
}
