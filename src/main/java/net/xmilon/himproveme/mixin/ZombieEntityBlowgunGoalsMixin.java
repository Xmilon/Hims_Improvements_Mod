package net.xmilon.himproveme.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.world.World;
import net.xmilon.himproveme.combat.BlowgunMobAttackHelper;
import net.xmilon.himproveme.entity.ai.goal.BlowgunAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.mob.ZombieEntity.class)
public abstract class ZombieEntityBlowgunGoalsMixin extends HostileEntity {
    protected ZombieEntityBlowgunGoalsMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    private void himproveme$addHuskBlowgunGoal(CallbackInfo ci) {
        if ((Object) this instanceof HuskEntity) {
            this.goalSelector.add(1, new BlowgunAttackGoal<>((HostileEntity & RangedAttackMob) this, 1.0D, BlowgunMobAttackHelper.ATTACK_RANGE));
        }
    }
}
