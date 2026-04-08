package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.SculkInvisibilityContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntitySculkInvisibilityMixin {
    @Inject(method = "dropItem", at = @At("HEAD"))
    private void himproveme$beginDropSuppression(
            ItemStack stack,
            boolean throwRandomly,
            boolean retainOwnership,
            CallbackInfoReturnable<Entity> cir
    ) {
        himproveme$pushSuppression();
    }

    @Inject(method = "dropItem", at = @At("RETURN"))
    private void himproveme$endDropSuppression(
            ItemStack stack,
            boolean throwRandomly,
            boolean retainOwnership,
            CallbackInfoReturnable<Entity> cir
    ) {
        himproveme$popSuppression();
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void himproveme$beginAttackSuppression(Entity target, CallbackInfo ci) {
        himproveme$pushSuppression();
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void himproveme$endAttackSuppression(Entity target, CallbackInfo ci) {
        himproveme$popSuppression();
    }

    private void himproveme$pushSuppression() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (PerkAccess.hasSculkInvisibility(player)) {
            SculkInvisibilityContext.push(player);
        }
    }

    private void himproveme$popSuppression() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (PerkAccess.hasSculkInvisibility(player)) {
            SculkInvisibilityContext.pop(player);
        }
    }
}
