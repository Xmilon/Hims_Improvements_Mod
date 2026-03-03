package net.xmilon.himproveme.item.custom.ability;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SpecialAbilityItem {
    void onSpecialAbilityTick(ServerPlayerEntity player, ItemStack stack);

    default void onSpecialAbilityActivated(ServerPlayerEntity player, ItemStack stack) {
    }

    default boolean keepsAbilityActive() {
        return true;
    }
}
