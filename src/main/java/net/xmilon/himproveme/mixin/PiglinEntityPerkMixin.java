package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.perk.NetherPerkHelper;
import net.xmilon.himproveme.perk.PerkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityPerkMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void himproveme$alertNearbyPiglinsWhenPiglinIsAttacked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PiglinEntity self = (PiglinEntity) (Object) this;
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player && PerkAccess.hasFriendlyPiglins(player)) {
            NetherPerkHelper.alertNearbyPiglins(self.getWorld(), self.getPos(), player, self);
        }
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void himproveme$openPiglinTradeMenu(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PiglinEntity self = (PiglinEntity) (Object) this;
        boolean canTrade = NetherPerkHelper.canTradeWith(player, self);
        if (self.getWorld().isClient) {
            if (canTrade) {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            return;
        }

        if (canTrade && player instanceof ServerPlayerEntity serverPlayer && NetherPerkHelper.tryOpenTrade(serverPlayer, self)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
