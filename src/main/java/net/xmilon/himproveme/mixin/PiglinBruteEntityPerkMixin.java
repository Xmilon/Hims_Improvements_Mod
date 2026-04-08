package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.xmilon.himproveme.perk.NetherPerkHelper;
import net.xmilon.himproveme.perk.PerkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBruteEntity.class)
public abstract class PiglinBruteEntityPerkMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void himproveme$alertNearbyPiglinsWhenBruteIsAttacked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PiglinBruteEntity self = (PiglinBruteEntity) (Object) this;
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player && PerkAccess.hasFriendlyPiglins(player)) {
            NetherPerkHelper.alertNearbyPiglins(self.getWorld(), self.getPos(), player, self);
        }
    }
}
