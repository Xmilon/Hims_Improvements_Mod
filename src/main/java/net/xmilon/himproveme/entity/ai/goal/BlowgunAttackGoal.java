package net.xmilon.himproveme.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.xmilon.himproveme.combat.BlowgunMobAttackHelper;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.BlowgunItem;

import java.util.EnumSet;

public class BlowgunAttackGoal<T extends HostileEntity & RangedAttackMob> extends Goal {
    private final T actor;
    private final double speed;
    private final float squaredRange;
    private int cooldown = -1;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;

    public BlowgunAttackGoal(T actor, double speed, float range) {
        this.actor = actor;
        this.speed = speed;
        this.squaredRange = range * range;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return this.actor.getTarget() != null && this.isHoldingBlowgun();
    }

    @Override
    public boolean shouldContinue() {
        return (this.canStart() || !this.actor.getNavigation().isIdle()) && this.isHoldingBlowgun();
    }

    @Override
    public void start() {
        super.start();
        this.actor.setAttacking(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.actor.setAttacking(false);
        this.targetSeeingTicker = 0;
        this.cooldown = -1;
        this.actor.clearActiveItem();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.actor.getTarget();
        if (target == null) {
            return;
        }

        double distance = this.actor.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        boolean canSee = this.actor.getVisibilityCache().canSee(target);
        boolean sawTargetLastTick = this.targetSeeingTicker > 0;
        if (canSee != sawTargetLastTick) {
            this.targetSeeingTicker = 0;
        }

        this.targetSeeingTicker += canSee ? 1 : -1;
        if (distance <= this.squaredRange && this.targetSeeingTicker >= 10) {
            this.actor.getNavigation().stop();
            this.combatTicks++;
        } else {
            this.actor.getNavigation().startMovingTo(target, this.speed);
            this.combatTicks = -1;
        }

        if (this.combatTicks >= 20) {
            if (this.actor.getRandom().nextFloat() < 0.3F) {
                this.movingToLeft = !this.movingToLeft;
            }
            if (this.actor.getRandom().nextFloat() < 0.3F) {
                this.backward = !this.backward;
            }
            this.combatTicks = 0;
        }

        if (this.combatTicks > -1) {
            if (distance > this.squaredRange * 0.75F) {
                this.backward = false;
            } else if (distance < this.squaredRange * 0.25F) {
                this.backward = true;
            }

            this.actor.getMoveControl().strafeTo(this.backward ? -0.35F : 0.35F, this.movingToLeft ? 0.35F : -0.35F);
            if (this.actor.getControllingVehicle() instanceof MobEntity vehicle) {
                vehicle.lookAtEntity(target, 30.0F, 30.0F);
            }

            this.actor.lookAtEntity(target, 30.0F, 30.0F);
        } else {
            this.actor.getLookControl().lookAt(target, 30.0F, 30.0F);
        }

        if (this.actor.isUsingItem()) {
            if (!canSee && this.targetSeeingTicker < -30) {
                this.actor.clearActiveItem();
            } else if (canSee && this.actor.getItemUseTime() >= BlowgunItem.AIM_RAISE_TICKS) {
                this.actor.clearActiveItem();
                this.actor.shootAt(target, 1.0F);
                this.cooldown = BlowgunMobAttackHelper.getShotCooldownTicks(this.actor, this.actor.getMainHandStack());
            }
        } else if (--this.cooldown <= 0 && this.targetSeeingTicker >= -30) {
            this.actor.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(this.actor, ModItem.BLOWGUN));
        }
    }

    private boolean isHoldingBlowgun() {
        return this.actor.isHolding(BlowgunItem::isBlowgun);
    }
}
