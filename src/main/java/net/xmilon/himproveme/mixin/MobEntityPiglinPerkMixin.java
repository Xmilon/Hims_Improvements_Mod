package net.xmilon.himproveme.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.perk.NetherPerkHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityPiglinPerkMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void himproveme$keepPiglinsNeutralUntilProvoked(@Nullable LivingEntity target, CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (self instanceof AbstractPiglinEntity piglin
                && target != null
                && NetherPerkHelper.shouldPiglinsIgnore(piglin, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$clearPiglinTargetsWhenNeutral(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        if (!(self instanceof AbstractPiglinEntity piglin)) {
            return;
        }

        LivingEntity target = piglin.getTarget();
        if (target != null && NetherPerkHelper.shouldPiglinsIgnore(piglin, target)) {
            piglin.setTarget(null);
            piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        }
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void himproveme$openBruteTradeMenu(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        MobEntity self = (MobEntity) (Object) this;
        if (!(self instanceof PiglinBruteEntity brute)) {
            return;
        }

        boolean canTrade = NetherPerkHelper.canTradeWith(player, brute);
        if (self.getWorld().isClient) {
            if (canTrade) {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            return;
        }

        if (canTrade && player instanceof ServerPlayerEntity serverPlayer && NetherPerkHelper.tryOpenTrade(serverPlayer, brute)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
