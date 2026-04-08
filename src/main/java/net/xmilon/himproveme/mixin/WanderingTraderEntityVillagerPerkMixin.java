package net.xmilon.himproveme.mixin;

import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.perk.PerkAccess;
import net.xmilon.himproveme.perk.VillagerPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityVillagerPerkMixin {
    @Inject(method = "interactMob", at = @At("HEAD"))
    private void himproveme$upgradeWanderingTraderOffers(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity serverPlayer && PerkAccess.hasTravellingTreasures(serverPlayer)) {
            VillagerPerkHelper.prepareWanderingTraderOffers((WanderingTraderEntity) (Object) this);
        }
    }
}
