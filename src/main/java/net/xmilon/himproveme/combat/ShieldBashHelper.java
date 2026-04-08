package net.xmilon.himproveme.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.xmilon.himproveme.enchantment.ModEnchantments;
import net.xmilon.himproveme.network.ShieldBashPayload;
import org.jetbrains.annotations.Nullable;

public final class ShieldBashHelper {
    private static final int BASE_BASH_COOLDOWN_TICKS = 70;
    private static final int COOLDOWN_REDUCTION_PER_LEVEL = 5;
    private static final float BASE_BASH_DAMAGE = 1.0F;
    private static final float BASE_BASH_KNOCKBACK = 0.75F;
    private static final float BASH_KNOCKBACK_PER_LEVEL = 0.20F;

    private ShieldBashHelper() {
    }

    public static boolean isShield(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShieldItem;
    }

    public static boolean canRaiseShield(PlayerEntity player, Hand hand) {
        ItemStack shieldStack = player.getStackInHand(hand);
        return player.isSneaking()
                && isShield(shieldStack)
                && !player.getItemCooldownManager().isCoolingDown(shieldStack.getItem());
    }

    public static int getBashingLevel(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        RegistryEntry<Enchantment> bashing = getBashingEntry(player.getWorld().getRegistryManager());
        if (bashing == null) {
            return 0;
        }

        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(bashing);
    }

    @Nullable
    public static Hand getActiveBashHand(PlayerEntity player) {
        if (!player.isBlocking() || !player.isSneaking()) {
            return null;
        }

        Hand activeHand = player.getActiveHand();
        ItemStack shieldStack = player.getStackInHand(activeHand);
        if (!isShield(shieldStack) || player.getItemCooldownManager().isCoolingDown(shieldStack.getItem())) {
            return null;
        }

        return getBashingLevel(player, shieldStack) > 0 ? activeHand : null;
    }

    public static int getBashCooldownTicks(int level) {
        return Math.max(45, BASE_BASH_COOLDOWN_TICKS - Math.max(0, level - 1) * COOLDOWN_REDUCTION_PER_LEVEL);
    }

    public static float getBashDamage(int level) {
        return BASE_BASH_DAMAGE * level;
    }

    public static float getBashKnockback(int level) {
        return BASE_BASH_KNOCKBACK + Math.max(0, level - 1) * BASH_KNOCKBACK_PER_LEVEL;
    }

    public static void performBash(ServerPlayerEntity player, Hand hand, int entityId) {
        if (player.getActiveHand() != hand) {
            return;
        }

        ItemStack shieldStack = player.getStackInHand(hand);
        if (!player.isBlocking()
                || !player.isSneaking()
                || !isShield(shieldStack)
                || player.getItemCooldownManager().isCoolingDown(shieldStack.getItem())) {
            return;
        }

        int level = getBashingLevel(player, shieldStack);
        if (level <= 0) {
            return;
        }

        DualWieldCombatHelper.lockAttacksAfterBash(player);
        player.stopUsingItem();
        player.getItemCooldownManager().set(shieldStack.getItem(), getBashCooldownTicks(level));
        player.swingHand(hand, true);
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.PLAYERS,
                0.8F,
                1.0F
        );

        if (entityId == ShieldBashPayload.SWING_ONLY_ENTITY_ID) {
            return;
        }

        Entity target = player.getServerWorld().getEntityById(entityId);
        if (!DualWieldCombatHelper.isValidOffhandTarget(player, target)) {
            return;
        }

        if (!player.getServerWorld().getWorldBorder().contains(target.getBlockPos())
                || !player.canInteractWithEntityIn(target.getBoundingBox(), 1.0D)) {
            return;
        }

        if (!target.damage(player.getDamageSources().playerAttack(player), getBashDamage(level))) {
            return;
        }

        double x = player.getX() - target.getX();
        double z = player.getZ() - target.getZ();
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.takeKnockback(getBashKnockback(level), x, z);
        } else {
            double knockback = getBashKnockback(level) * 0.5D;
            double horizontalDistance = Math.max(1.0E-4D, Math.sqrt(x * x + z * z));
            target.addVelocity((-x / horizontalDistance) * knockback, 0.1D, (-z / horizontalDistance) * knockback);
        }

        player.getWorld().playSound(
                null,
                target.getBlockPos(),
                SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                SoundCategory.PLAYERS,
                0.6F,
                1.0F
        );
    }

    @Nullable
    private static RegistryEntry<Enchantment> getBashingEntry(DynamicRegistryManager registryManager) {
        return registryManager.getOptional(RegistryKeys.ENCHANTMENT)
                .flatMap(registry -> registry.getEntry(ModEnchantments.BASHING))
                .orElse(null);
    }
}
