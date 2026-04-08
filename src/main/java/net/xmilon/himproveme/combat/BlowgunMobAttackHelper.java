package net.xmilon.himproveme.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.item.custom.BlowgunItem;
import net.xmilon.himproveme.item.custom.BlowgunProjectileAccess;

public final class BlowgunMobAttackHelper {
    public static final float ATTACK_RANGE = 13.0F;
    private static final double FIREBALL_SPEED = 0.9D;
    private static final double FIREBALL_ACCELERATION = 0.02D;

    private BlowgunMobAttackHelper() {
    }

    public static int getShotCooldownTicks(LivingEntity shooter, ItemStack blowgunStack) {
        ItemStack ammoStack = BlowgunShotHelper.hasExplosiveBlow(shooter, blowgunStack)
                ? new ItemStack(Items.FIRE_CHARGE)
                : new ItemStack(Items.ARROW);
        return BlowgunShotHelper.getShotCooldownTicks(shooter, blowgunStack, ammoStack);
    }

    public static boolean shoot(ServerWorld world, LivingEntity shooter, LivingEntity target, ItemStack blowgunStack) {
        if (!BlowgunItem.isBlowgun(blowgunStack)) {
            return false;
        }

        if (BlowgunShotHelper.hasExplosiveBlow(shooter, blowgunStack)) {
            world.spawnEntity(createFireball(world, shooter, target));
            playExplosiveSounds(world, shooter);
            return true;
        }

        world.spawnEntity(createDartProjectile(shooter, target, blowgunStack));
        playDartSounds(world, shooter);
        return true;
    }

    private static PersistentProjectileEntity createDartProjectile(LivingEntity shooter, LivingEntity target, ItemStack blowgunStack) {
        ArrowItem arrowItem = (ArrowItem) Items.ARROW;
        PersistentProjectileEntity projectile = arrowItem.createArrow(shooter.getWorld(), new ItemStack(Items.ARROW), shooter, blowgunStack);
        double x = target.getX() - shooter.getX();
        double y = target.getBodyY(0.3333333333333333D) - projectile.getY();
        double z = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(x * x + z * z);
        projectile.setVelocity(x, y + horizontal * 0.12D, z, BlowgunItem.SHOT_SPEED, 1.75F);
        projectile.setDamage(BlowgunItem.BASE_DAMAGE);
        projectile.setCritical(false);

        if (projectile instanceof BlowgunProjectileAccess blowgunProjectile) {
            blowgunProjectile.himproveme$markBlowgun(
                    shooter.getX(),
                    shooter.getEyeY(),
                    shooter.getZ(),
                    BlowgunItem.STRAIGHT_DISTANCE,
                    BlowgunItem.MAX_DISTANCE
            );
        }

        return projectile;
    }

    private static SmallFireballEntity createFireball(ServerWorld world, LivingEntity shooter, LivingEntity target) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getBodyY(0.3333333333333333D), target.getZ());
        Vec3d direction = targetPos.subtract(shooter.getEyePos()).normalize();
        Vec3d spawnPos = shooter.getEyePos().add(direction.multiply(0.45D));
        SmallFireballEntity fireball = new SmallFireballEntity(world, shooter, direction);
        fireball.setPosition(spawnPos.x, spawnPos.y - 0.12D, spawnPos.z);
        fireball.setVelocity(direction.multiply(FIREBALL_SPEED));
        fireball.accelerationPower = FIREBALL_ACCELERATION;
        fireball.setItem(new ItemStack(Items.FIRE_CHARGE));
        return fireball;
    }

    private static void playDartSounds(ServerWorld world, LivingEntity shooter) {
        world.playSound(
                null,
                shooter.getX(),
                shooter.getEyeY(),
                shooter.getZ(),
                SoundEvents.ENTITY_LLAMA_SPIT,
                SoundCategory.HOSTILE,
                0.28F,
                1.1F + world.random.nextFloat() * 0.08F
        );
        world.playSound(
                null,
                shooter.getX(),
                shooter.getEyeY(),
                shooter.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.HOSTILE,
                0.22F,
                1.35F + world.random.nextFloat() * 0.08F
        );
    }

    private static void playExplosiveSounds(ServerWorld world, LivingEntity shooter) {
        world.playSound(
                null,
                shooter.getX(),
                shooter.getEyeY(),
                shooter.getZ(),
                SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.HOSTILE,
                0.55F,
                1.0F + world.random.nextFloat() * 0.08F
        );
        world.playSound(
                null,
                shooter.getX(),
                shooter.getEyeY(),
                shooter.getZ(),
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.HOSTILE,
                0.28F,
                1.08F + world.random.nextFloat() * 0.08F
        );
    }
}
