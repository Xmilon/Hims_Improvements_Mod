package net.xmilon.himproveme.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.xmilon.himproveme.access.VillagerJobApplicationAccess;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.VillagerPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityVillagerPerkMixin implements VillagerJobApplicationAccess {
    @Unique
    private int himproveme$jobApplicationPanicTicks;
    @Unique
    private Vec3d himproveme$jobApplicationPanicSource = Vec3d.ZERO;

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void himproveme$handleJobApplication(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = VillagerPerkHelper.tryUseJobApplication(player, (VillagerEntity) (Object) this, hand);
        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
        }
    }

    @Inject(
            method = "beginTradeWith",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/VillagerEntity;sendOffers(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;I)V"
            )
    )
    private void himproveme$prepareVillagerPerkTrades(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer && PerkAccess.hasMarketConnections(serverPlayer)) {
            VillagerPerkHelper.prepareVillagerForTrading((VillagerEntity) (Object) this, serverPlayer);
        }
    }

    @Inject(method = "beginTradeWith", at = @At("TAIL"))
    private void himproveme$syncVillagerTradeStatus(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer && PerkAccess.hasMarketConnections(serverPlayer)) {
            VillagerPerkHelper.syncVillagerTradeStatus((VillagerEntity) (Object) this, serverPlayer);
        }
    }

    @Inject(method = "afterUsing", at = @At("TAIL"))
    private void himproveme$applyVillagerTradeCooldown(TradeOffer offer, CallbackInfo ci) {
        VillagerPerkHelper.onVillagerTradeUsed((VillagerEntity) (Object) this, offer);
    }

    @Inject(method = "restock", at = @At("TAIL"))
    private void himproveme$reapplyVillagerTradeCooldowns(CallbackInfo ci) {
        VillagerPerkHelper.onVillagerRestock((VillagerEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void himproveme$tickVillagerPerkState(CallbackInfo ci) {
        VillagerPerkHelper.tickVillager((VillagerEntity) (Object) this);
    }

    @Override
    public void himproveme$startJobApplicationPanic(Vec3d sourcePos) {
        himproveme$jobApplicationPanicTicks = VillagerPerkHelper.JOB_APPLICATION_PANIC_TICKS;
        himproveme$jobApplicationPanicSource = sourcePos;
    }

    @Override
    public int himproveme$getJobApplicationPanicTicks() {
        return himproveme$jobApplicationPanicTicks;
    }

    @Override
    public void himproveme$setJobApplicationPanicTicks(int ticks) {
        himproveme$jobApplicationPanicTicks = Math.max(0, ticks);
    }

    @Override
    public Vec3d himproveme$getJobApplicationPanicSource() {
        return himproveme$jobApplicationPanicSource;
    }
}
