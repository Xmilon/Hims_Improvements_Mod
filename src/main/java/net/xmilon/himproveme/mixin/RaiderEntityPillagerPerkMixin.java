package net.xmilon.himproveme.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.xmilon.himproveme.access.DomesticatedRavagerAccess;
import net.xmilon.himproveme.access.RaiderPerkAggroAccess;
import net.xmilon.himproveme.perk.DomesticatedRavagerTrackedData;
import net.xmilon.himproveme.perk.PillagerPerkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(RaiderEntity.class)
public abstract class RaiderEntityPillagerPerkMixin implements RaiderPerkAggroAccess {
    @Unique
    private UUID himproveme$angryPlayerUuid;
    @Unique
    private int himproveme$angryUntilAge;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void himproveme$initDomesticatedRavagerTracker(DataTracker.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof RavagerEntity) {
            builder.add(DomesticatedRavagerTrackedData.DOMESTICATED, false);
            builder.add(DomesticatedRavagerTrackedData.OWNER_UUID, java.util.Optional.empty());
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void himproveme$markPlayerAsProvokedWhenDamagingRaider(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        RaiderEntity self = (RaiderEntity) (Object) this;
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player) {
            if (self instanceof RavagerEntity ravager && ((DomesticatedRavagerAccess) ravager).himproveme$isOwnedBy(player)) {
                return;
            }
            PillagerPerkHelper.alertNearbyRaiders(self.getWorld(), self.getPos(), player, self);
        }
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void himproveme$clearForbiddenTargets(CallbackInfo ci) {
        RaiderEntity self = (RaiderEntity) (Object) this;
        LivingEntity target = self.getTarget();
        if (himproveme$angryPlayerUuid != null && !himproveme$hasActivePillagerAnger()) {
            himproveme$clearPillagerAnger();
        }
        if (target == null) {
            return;
        }

        if (PillagerPerkHelper.shouldRaidersIgnore(self, target)) {
            self.setTarget(null);
            self.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            return;
        }

        if (self instanceof RavagerEntity ravager
                && ((DomesticatedRavagerAccess) ravager).himproveme$isDomesticated()
                && ((DomesticatedRavagerAccess) ravager).himproveme$getOwnerUuid() != null
                && ((DomesticatedRavagerAccess) ravager).himproveme$getOwnerUuid().equals(target.getUuid())) {
            self.setTarget(null);
            self.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        }
    }

    @Override
    public void himproveme$angerAt(PlayerEntity player, int durationTicks) {
        RaiderEntity self = (RaiderEntity) (Object) this;
        this.himproveme$angryPlayerUuid = player.getUuid();
        this.himproveme$angryUntilAge = Math.max(this.himproveme$angryUntilAge, self.age + durationTicks);
    }

    @Override
    public boolean himproveme$isAngryAt(PlayerEntity player) {
        return this.himproveme$angryPlayerUuid != null
                && this.himproveme$angryPlayerUuid.equals(player.getUuid())
                && himproveme$hasActivePillagerAnger();
    }

    @Override
    public boolean himproveme$hasActivePillagerAnger() {
        RaiderEntity self = (RaiderEntity) (Object) this;
        return this.himproveme$angryPlayerUuid != null && self.age < this.himproveme$angryUntilAge;
    }

    @Override
    public void himproveme$clearPillagerAnger() {
        this.himproveme$angryPlayerUuid = null;
        this.himproveme$angryUntilAge = 0;
    }
}
