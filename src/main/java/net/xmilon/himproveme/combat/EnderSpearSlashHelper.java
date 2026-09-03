package net.xmilon.himproveme.combat;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.access.EnderSpearSlashAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EnderSpearSlashHelper {
    public static final Identifier DOUBLE_ENDER_SPEAR_ID = Identifier.of(HimProveMe.MOD_ID, "double_ender_spear");
    public static final int ANIMATION_TICKS = 12;
    public static final int COOLDOWN_TICKS = 40;
    public static final byte MAIN_HAND_STATUS = 61;
    public static final byte OFF_HAND_STATUS = 62;

    private static final double SLASH_RANGE = 6.25D;
    private static final double SLASH_RANGE_SQUARED = SLASH_RANGE * SLASH_RANGE;
    private static final double SLASH_WIDTH = 2.35D;
    private static final double MAX_VERTICAL_OFFSET = 1.9D;
    private static final double MIN_ARC_DOT = 0.25D;
    private static final float DAMAGE_MULTIPLIER = 0.9F;
    private static final float FLAT_DAMAGE_PENALTY = 1.0F;
    private static final float KNOCKBACK = 0.6F;

    private EnderSpearSlashHelper() {
    }

    public static boolean isDoubleEnderSpear(ItemStack stack) {
        return !stack.isEmpty() && DOUBLE_ENDER_SPEAR_ID.equals(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()));
    }

    public static boolean activateSlash(PlayerEntity player, Hand hand, ItemStack stack) {
        if (hand != Hand.MAIN_HAND || !isDoubleEnderSpear(stack) || player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            return false;
        }

        player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
        startSlashAnimation(player, hand);

        if (player.getWorld() instanceof ServerWorld serverWorld) {
            performSlashAttack(serverWorld, player, stack);
            player.getWorld().sendEntityStatus(player, hand == Hand.MAIN_HAND ? MAIN_HAND_STATUS : OFF_HAND_STATUS);
        }

        return true;
    }

    public static void startSlashAnimation(LivingEntity entity, Hand hand) {
        if (entity instanceof EnderSpearSlashAccess access) {
            access.himproveme$startEnderSpearSlash(hand);
        }
    }

    public static boolean isSlashActive(LivingEntity entity, Hand hand) {
        return entity instanceof EnderSpearSlashAccess access && access.himproveme$isEnderSpearSlashActive(hand);
    }

    public static float getSlashProgress(LivingEntity entity, Hand hand, float tickDelta) {
        if (entity instanceof EnderSpearSlashAccess access) {
            return access.himproveme$getEnderSpearSlashProgress(hand, tickDelta);
        }
        return 0.0F;
    }

    public static SlashAnimation getSlashAnimation(LivingEntity entity, Hand hand, float tickDelta) {
        float progress = getSlashProgress(entity, hand, tickDelta);
        if (progress <= 0.0F || progress >= 1.0F) {
            return SlashAnimation.NONE;
        }

        float windup = easeOutCubic(phase(progress, 0.0F, 0.24F));
        float strike = easeOutCubic(phase(progress, 0.18F, 0.52F));
        float overswing = easeOutBack(phase(progress, 0.32F, 0.78F));
        float recovery = easeOutCubic(phase(progress, 0.52F, 1.0F));
        return new SlashAnimation(progress, windup, strike, overswing, recovery);
    }

    private static void performSlashAttack(ServerWorld world, PlayerEntity player, ItemStack stack) {
        float baseDamage = Math.max(0.0F, (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * DAMAGE_MULTIPLIER - FLAT_DAMAGE_PENALTY);
        List<Entity> targets = collectTargets(player);

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.9F, 0.95F);
        player.spawnSweepAttackParticles();

        for (Entity target : targets) {
            damageTarget(world, player, stack, target, baseDamage);
        }

        player.addExhaustion(0.1F);
    }

    private static List<Entity> collectTargets(PlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVector();
        Vec3d flatLook = new Vec3d(look.x, 0.0D, look.z);
        if (flatLook.lengthSquared() < 1.0E-4D) {
            flatLook = new Vec3d(0.0D, 0.0D, 1.0D);
        }
        Vec3d normalizedFlatLook = flatLook.normalize();

        Box searchBox = player.getBoundingBox().stretch(look.multiply(SLASH_RANGE)).expand(SLASH_WIDTH, 1.25D, SLASH_WIDTH);
        List<Entity> targets = new ArrayList<>(player.getWorld().getOtherEntities(player, searchBox, entity -> canHitTarget(player, entity)));
        targets.removeIf(entity -> !isInsideSlashArc(player, entity, eyePos, normalizedFlatLook));
        targets.sort(Comparator.comparingDouble(player::squaredDistanceTo));
        return targets;
    }

    private static boolean canHitTarget(PlayerEntity attacker, Entity target) {
        if (!target.canBeHitByProjectile() || !target.isAlive() || target.isInvulnerable()) {
            return false;
        }
        if (target instanceof PlayerEntity playerTarget && !attacker.shouldDamagePlayer(playerTarget)) {
            return false;
        }
        return !attacker.isConnectedThroughVehicle(target);
    }

    private static boolean isInsideSlashArc(PlayerEntity player, Entity target, Vec3d eyePos, Vec3d flatLook) {
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        if (eyePos.squaredDistanceTo(targetCenter) > SLASH_RANGE_SQUARED) {
            return false;
        }
        if (Math.abs(targetCenter.y - eyePos.y) > MAX_VERTICAL_OFFSET + target.getHeight() * 0.5D) {
            return false;
        }
        if (!player.canSee(target)) {
            return false;
        }

        Vec3d horizontalDelta = new Vec3d(targetCenter.x - eyePos.x, 0.0D, targetCenter.z - eyePos.z);
        if (horizontalDelta.lengthSquared() < 1.0E-4D) {
            return true;
        }

        double arcDot = flatLook.dotProduct(horizontalDelta.normalize());
        return arcDot >= MIN_ARC_DOT;
    }

    private static boolean damageTarget(ServerWorld world, PlayerEntity player, ItemStack stack, Entity target, float baseDamage) {
        DamageSource damageSource = player.getDamageSources().playerAttack(player);
        float enchantmentDamage = EnchantmentHelper.getDamage(world, stack, target, damageSource, baseDamage) - baseDamage;
        float totalDamage = baseDamage + Math.max(0.0F, enchantmentDamage);
        Entity damagedEntity = target instanceof EnderDragonPart dragonPart ? dragonPart.owner : target;
        float previousHealth = damagedEntity instanceof LivingEntity living ? living.getHealth() : 0.0F;

        if (!target.damage(damageSource, totalDamage)) {
            return false;
        }

        if (damagedEntity instanceof LivingEntity livingTarget) {
            boolean shouldDamageItem = stack.postHit(livingTarget, player);
            if (shouldDamageItem) {
                stack.postDamageEntity(livingTarget, player);
            }
            livingTarget.takeKnockback(KNOCKBACK, player.getX() - target.getX(), player.getZ() - target.getZ());
            float damageDealt = previousHealth - livingTarget.getHealth();
            if (damageDealt > 0.0F) {
                player.increaseStat(net.minecraft.stat.Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
            }
        } else {
            Vec3d delta = target.getPos().subtract(player.getPos());
            double horizontalDistance = Math.max(1.0E-4D, Math.sqrt(delta.x * delta.x + delta.z * delta.z));
            target.addVelocity(delta.x / horizontalDistance * 0.25D, 0.08D, delta.z / horizontalDistance * 0.25D);
        }

        EnchantmentHelper.onTargetDamaged(world, target, damageSource, stack);
        if (enchantmentDamage > 0.0F) {
            player.addEnchantedHitParticles(target);
        }
        player.onAttacking(target);
        return true;
    }

    private static float phase(float progress, float start, float end) {
        if (end <= start) {
            return 1.0F;
        }
        return MathHelper.clamp((progress - start) / (end - start), 0.0F, 1.0F);
    }

    private static float easeOutCubic(float value) {
        float inverse = 1.0F - value;
        return 1.0F - inverse * inverse * inverse;
    }

    private static float easeOutBack(float value) {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float inverse = value - 1.0F;
        return 1.0F + c3 * inverse * inverse * inverse + c1 * inverse * inverse;
    }

    public record SlashAnimation(float progress, float windup, float strike, float overswing, float recovery) {
        public static final SlashAnimation NONE = new SlashAnimation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        public boolean active() {
            return progress > 0.0F && progress < 1.0F;
        }
    }
}
