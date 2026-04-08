package net.xmilon.himproveme.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.xmilon.himproveme.enchantment.ModEnchantments;
import net.xmilon.himproveme.item.custom.DaggerItem;

import java.util.Optional;

public final class DaggerGripHelper {
    private DaggerGripHelper() {
    }

    public static boolean hasDualGripDaggers(PlayerEntity player) {
        return DaggerItem.isDagger(player.getMainHandStack())
                && DaggerItem.isDagger(player.getOffHandStack())
                && getGripLevel(player, player.getMainHandStack()) > 0
                && getGripLevel(player, player.getOffHandStack()) > 0
                && DualWieldCombatHelper.canUseOffhandAttack(player);
    }

    public static int getGripLevel(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        return getGripLevel(entity.getWorld().getRegistryManager(), stack);
    }

    public static int getGripLevel(DynamicRegistryManager registryManager, ItemStack stack) {
        Optional<RegistryEntry.Reference<Enchantment>> gripEntry = registryManager.getOptional(RegistryKeys.ENCHANTMENT)
                .flatMap(registry -> registry.getEntry(ModEnchantments.GRIP));
        if (gripEntry.isEmpty()) {
            return 0;
        }

        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(gripEntry.get());
    }
}
