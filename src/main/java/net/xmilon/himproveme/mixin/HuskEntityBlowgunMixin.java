package net.xmilon.himproveme.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.xmilon.himproveme.combat.BlowgunMobAttackHelper;
import net.xmilon.himproveme.item.ModItem;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HuskEntity.class)
public abstract class HuskEntityBlowgunMixin extends ZombieEntity implements RangedAttackMob {
    protected HuskEntityBlowgunMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlowgunMobAttackHelper.shoot(serverWorld, this, target, this.getMainHandStack());
        }
    }

    @Override
    public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
        return weapon == ModItem.BLOWGUN || super.canUseRangedWeapon(weapon);
    }
}
