package net.xmilon.himproveme.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.xmilon.himproveme.enchantment.ModEnchantments;
import net.xmilon.himproveme.item.custom.BlowgunItem;

public final class BlowgunShotHelper {
    private static final int FAST_BLOW_COOLDOWN_REDUCTION = 4;
    private static final int MIN_FAST_BLOW_COOLDOWN_TICKS = 8;
    private static final int EXPLOSIVE_ARROW_COOLDOWN_TICKS = 30;
    private static final int EXPLOSIVE_FIREBALL_COOLDOWN_TICKS = 36;
    private static final int EXPLOSIVE_ROCKET_COOLDOWN_TICKS = 46;

    private BlowgunShotHelper() {
    }

    public static int getFastBlowLevel(LivingEntity entity, ItemStack stack) {
        return getLevel(entity.getWorld().getRegistryManager(), stack, ModEnchantments.FAST_BLOW);
    }

    public static int getExplosiveBlowLevel(LivingEntity entity, ItemStack stack) {
        return getLevel(entity.getWorld().getRegistryManager(), stack, ModEnchantments.EXPLOSIVE_BLOW);
    }

    public static boolean hasExplosiveBlow(LivingEntity entity, ItemStack stack) {
        return getExplosiveBlowLevel(entity, stack) > 0;
    }

    public static int getShotCooldownTicks(LivingEntity entity, ItemStack blowgunStack, ItemStack ammoStack) {
        if (hasExplosiveBlow(entity, blowgunStack)) {
            if (isFireChargeAmmo(ammoStack)) {
                return EXPLOSIVE_FIREBALL_COOLDOWN_TICKS;
            }
            if (isFireworkRocketAmmo(ammoStack)) {
                return EXPLOSIVE_ROCKET_COOLDOWN_TICKS;
            }
            return EXPLOSIVE_ARROW_COOLDOWN_TICKS;
        }

        int fastBlowLevel = getFastBlowLevel(entity, blowgunStack);
        return Math.max(MIN_FAST_BLOW_COOLDOWN_TICKS, BlowgunItem.FIRE_INTERVAL_TICKS - fastBlowLevel * FAST_BLOW_COOLDOWN_REDUCTION);
    }

    public static ItemStack getAmmo(PlayerEntity player, ItemStack blowgunStack) {
        boolean explosiveBlow = hasExplosiveBlow(player, blowgunStack);
        if (explosiveBlow) {
            ItemStack heldExplosiveAmmo = getHeldAmmo(player, stack -> isFireChargeAmmo(stack) || isFireworkRocketAmmo(stack));
            if (!heldExplosiveAmmo.isEmpty()) {
                return heldExplosiveAmmo;
            }
        }

        ItemStack heldArrowAmmo = RangedWeaponItem.getHeldProjectile(player, RangedWeaponItem.BOW_PROJECTILES);
        if (!heldArrowAmmo.isEmpty()) {
            return heldArrowAmmo;
        }

        ItemStack inventoryArrowAmmo = findInventoryAmmo(player, RangedWeaponItem.BOW_PROJECTILES);
        if (!inventoryArrowAmmo.isEmpty()) {
            return inventoryArrowAmmo;
        }

        if (explosiveBlow) {
            ItemStack inventoryFireCharge = findInventoryAmmo(player, BlowgunShotHelper::isFireChargeAmmo);
            if (!inventoryFireCharge.isEmpty()) {
                return inventoryFireCharge;
            }

            ItemStack inventoryRocket = findInventoryAmmo(player, BlowgunShotHelper::isFireworkRocketAmmo);
            if (!inventoryRocket.isEmpty()) {
                return inventoryRocket;
            }
        }

        return player.getAbilities().creativeMode ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    public static boolean isFireChargeAmmo(ItemStack stack) {
        return stack.isOf(Items.FIRE_CHARGE);
    }

    public static boolean isFireworkRocketAmmo(ItemStack stack) {
        return stack.isOf(Items.FIREWORK_ROCKET);
    }

    private static ItemStack getHeldAmmo(PlayerEntity player, java.util.function.Predicate<ItemStack> predicate) {
        return RangedWeaponItem.getHeldProjectile(player, predicate);
    }

    private static ItemStack findInventoryAmmo(PlayerEntity player, java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack inventoryStack = player.getInventory().getStack(i);
            if (predicate.test(inventoryStack)) {
                return inventoryStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static int getLevel(DynamicRegistryManager registryManager, ItemStack stack, RegistryKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty()) {
            return 0;
        }

        RegistryEntry<Enchantment> enchantment = registryManager.getOptional(RegistryKeys.ENCHANTMENT)
                .flatMap(registry -> registry.getEntry(enchantmentKey))
                .orElse(null);
        if (enchantment == null) {
            return 0;
        }

        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(enchantment);
    }
}
