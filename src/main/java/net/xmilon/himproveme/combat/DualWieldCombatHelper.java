package net.xmilon.himproveme.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.xmilon.himproveme.access.DualWieldAttackAccess;
import net.xmilon.himproveme.access.DualWieldTargetAccess;
import net.xmilon.himproveme.compat.SpearBackportCompat;
import net.xmilon.himproveme.item.custom.DaggerItem;
import net.xmilon.himproveme.util.ModTags;

public final class DualWieldCombatHelper {
    private static final int READY_OFFHAND_TICKS = 1_000;
    private static final double MIN_ATTACK_SPEED = 0.1D;
    private static final double OFFHAND_COOLDOWN_MULTIPLIER = 1.7D;
    private static final int BASH_ATTACK_LOCK_TICKS = 4;

    private DualWieldCombatHelper() {
    }

    public static boolean canUseOffhandAttack(PlayerEntity player) {
        return !player.isSpectator()
                && isDualWieldWeapon(player.getOffHandStack())
                && (player.getMainHandStack().isEmpty()
                || isDualWieldWeapon(player.getMainHandStack())
                || ShieldBashHelper.isShield(player.getMainHandStack()));
    }

    public static boolean isOffhandOnlyAttack(PlayerEntity player) {
        return canUseOffhandAttack(player) && player.getMainHandStack().isEmpty();
    }

    public static boolean shouldUseOffhandAttackAsPrimary(PlayerEntity player) {
        return canUseOffhandAttack(player)
                && !isBashAttackLocked(player)
                && !player.isUsingItem()
                && (player.getMainHandStack().isEmpty() || ShieldBashHelper.isShield(player.getMainHandStack()));
    }

    public static boolean isOffhandAttackReady(PlayerEntity player) {
        return getOffhandAttackCooldownProgress(player, 0.5f) >= 1.0f;
    }

    public static boolean isValidOffhandTarget(PlayerEntity player, Entity target) {
        return target != null && target != player && target.isAttackable() && target.isAlive();
    }

    public static boolean isOffhandTargetInRange(PlayerEntity player, Entity target) {
        return isTargetInRange(player, player.getOffHandStack(), target);
    }

    public static boolean isTargetInRange(PlayerEntity player, ItemStack weaponStack, Entity target) {
        if (target == null) {
            return false;
        }

        double reach = getWeaponRange(player, weaponStack);
        return target.getBoundingBox().squaredMagnitude(player.getEyePos()) < reach * reach;
    }

    public static double getWeaponRange(PlayerEntity player, ItemStack weaponStack) {
        if (DaggerItem.isDagger(weaponStack)) {
            return DaggerItem.DEFAULT_RANGE;
        }

        if (SpearBackportCompat.hasPiercingWeapon(weaponStack)) {
            return SpearBackportCompat.getMaxAttackReach(weaponStack);
        }

        return player.getEntityInteractionRange();
    }

    public static float getOffhandAttackCooldownProgress(PlayerEntity player, float baseTime) {
        int lastAttackedTicks = getOffhandLastAttackedTicks(player);
        float cooldownPerTick = (float) ((20.0D / getOffhandAttackSpeed(player)) * OFFHAND_COOLDOWN_MULTIPLIER);
        return MathHelper.clamp((lastAttackedTicks + baseTime) / cooldownPerTick, 0.0f, 1.0f);
    }

    public static void startOffhandAttack(PlayerEntity player) {
        if (player instanceof DualWieldAttackAccess access) {
            access.himproveme$resetOffhandLastAttackedTicks();
            access.himproveme$startOffhandSwing();
        }
    }

    public static void startOffhandSwing(PlayerEntity player) {
        if (player instanceof DualWieldAttackAccess access) {
            access.himproveme$startOffhandSwing();
        }
    }

    public static boolean isOffhandSwingActive(PlayerEntity player) {
        return player instanceof DualWieldAttackAccess access && access.himproveme$isOffhandSwingActive();
    }

    public static void lockAttacksAfterBash(PlayerEntity player) {
        if (player instanceof DualWieldAttackAccess access) {
            access.himproveme$setBashAttackLockTicks(BASH_ATTACK_LOCK_TICKS);
        }
    }

    public static boolean isBashAttackLocked(PlayerEntity player) {
        return player instanceof DualWieldAttackAccess access && access.himproveme$hasBashAttackLock();
    }

    public static double getOffhandAttackDamage(PlayerEntity player) {
        double currentDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        return Math.max(0.0D, currentDamage
                - getMainHandAttributeAddValue(player.getMainHandStack(), EntityAttributes.GENERIC_ATTACK_DAMAGE)
                + getMainHandAttributeAddValue(player.getOffHandStack(), EntityAttributes.GENERIC_ATTACK_DAMAGE));
    }

    public static void resetOffhandAttackTicks(PlayerEntity player) {
        if (player instanceof DualWieldAttackAccess access) {
            access.himproveme$resetOffhandLastAttackedTicks();
        }
    }

    public static int getReadyOffhandTicks() {
        return READY_OFFHAND_TICKS;
    }

    public static boolean isDualWieldWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ModTags.Items.DUAL_WIELDABLE);
    }

    public static void prepareTargetForOffhandAttack(Entity target) {
        if (target instanceof LivingEntity livingEntity && livingEntity instanceof DualWieldTargetAccess access) {
            access.himproveme$resetDualWieldDamageImmunity();
        }
    }

    private static int getOffhandLastAttackedTicks(PlayerEntity player) {
        if (player instanceof DualWieldAttackAccess access) {
            return access.himproveme$getOffhandLastAttackedTicks();
        }
        return READY_OFFHAND_TICKS;
    }

    private static double getOffhandAttackSpeed(PlayerEntity player) {
        double currentAttackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
        double adjustedAttackSpeed = currentAttackSpeed
                - getMainHandAttributeAddValue(player.getMainHandStack(), EntityAttributes.GENERIC_ATTACK_SPEED)
                + getMainHandAttributeAddValue(player.getOffHandStack(), EntityAttributes.GENERIC_ATTACK_SPEED);
        return Math.max(MIN_ATTACK_SPEED, adjustedAttackSpeed);
    }

    private static double getMainHandAttributeAddValue(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        double[] total = {0.0D};
        stack.applyAttributeModifiers(EquipmentSlot.MAINHAND, (currentAttribute, modifier) -> {
            if (currentAttribute.equals(attribute) && modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.value();
            }
        });
        return total[0];
    }
}
