package net.xmilon.himproveme.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.xmilon.himproveme.combat.BlowgunMobAttackHelper;
import net.xmilon.himproveme.entity.ai.goal.BlowgunAttackGoal;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityBlowgunMixin extends HostileEntity {
    @Shadow
    @Final
    private BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal;

    @Shadow
    @Final
    private MeleeAttackGoal meleeAttackGoal;

    @Unique
    private BlowgunAttackGoal<AbstractSkeletonEntity> himproveme$blowgunAttackGoal;

    protected AbstractSkeletonEntityBlowgunMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updateAttackType", at = @At("HEAD"), cancellable = true)
    private void himproveme$useBlowgunAttackGoal(CallbackInfo ci) {
        if (this.himproveme$blowgunAttackGoal != null) {
            this.goalSelector.remove(this.himproveme$blowgunAttackGoal);
        }

        ItemStack mainHandStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!BlowgunItem.isBlowgun(mainHandStack) || this.getWorld() == null || this.getWorld().isClient) {
            return;
        }

        this.goalSelector.remove(this.meleeAttackGoal);
        this.goalSelector.remove(this.bowAttackGoal);
        this.goalSelector.add(4, this.himproveme$getBlowgunAttackGoal());
        ci.cancel();
    }

    @Inject(method = "shootAt", at = @At("HEAD"), cancellable = true)
    private void himproveme$shootBlowgun(LivingEntity target, float pullProgress, CallbackInfo ci) {
        ItemStack mainHandStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!(this.getWorld() instanceof ServerWorld serverWorld) || !BlowgunItem.isBlowgun(mainHandStack)) {
            return;
        }

        if (BlowgunMobAttackHelper.shoot(serverWorld, this, target, mainHandStack)) {
            ci.cancel();
        }
    }

    @Inject(method = "canUseRangedWeapon", at = @At("HEAD"), cancellable = true)
    private void himproveme$allowBlowgun(RangedWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
        if (weapon == ModItem.BLOWGUN) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private BlowgunAttackGoal<AbstractSkeletonEntity> himproveme$getBlowgunAttackGoal() {
        if (this.himproveme$blowgunAttackGoal == null) {
            this.himproveme$blowgunAttackGoal = new BlowgunAttackGoal<>((AbstractSkeletonEntity) (Object) this, 1.0D, BlowgunMobAttackHelper.ATTACK_RANGE);
        }
        return this.himproveme$blowgunAttackGoal;
    }
}
