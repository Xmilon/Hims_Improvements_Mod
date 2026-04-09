package net.xmilon.himproveme.enchantment;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xmilon.himproveme.perk.warden.WardenPerkHelper;

/**
 * Shared runtime for the once-per-day self-sepuku sword enchantment.
 */
public final class SelfSepukuEnchantmentHelper {
    private static final ComponentType<NbtComponent> CUSTOM_DATA = DataComponentTypes.CUSTOM_DATA;
    private static final String SELF_SEPUKU_DATA_KEY = "himproveme_self_sepuku";
    private static final String LAST_USED_DAY_KEY = "LastUsedDay";
    private static final long DAY_TICKS = 24000L;
    private static final int ANIMATION_DURATION_TICKS = 24;
    private static final int BUFF_DURATION_TICKS = 600;
    private static final float HEALTH_COST = 12.0F;

    private SelfSepukuEnchantmentHelper() {
    }

    /**
     * Uses the enchantment on the held sword, returning true once the input has been fully handled.
     */
    public static boolean tryUseAbility(ServerPlayerEntity player, ItemStack stack) {
        if (getLevel(player, stack) <= 0) {
            return false;
        }

        long currentDay = getCurrentWorldDay(player);
        if (getLastUsedDay(stack) == currentDay) {
            player.sendMessage(
                    Text.translatable("ability.himproveme.self_sepuku.cooldown", formatDuration(getTicksUntilNextDay(player)))
                            .formatted(Formatting.YELLOW),
                    true
            );
            return true;
        }

        setLastUsedDay(stack, currentDay);
        WardenPerkHelper.playSepukuAnimation(player, ANIMATION_DURATION_TICKS);
        player.damage(player.getDamageSources().magic(), HEALTH_COST);

        if (player.isAlive()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, BUFF_DURATION_TICKS, 0, true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, BUFF_DURATION_TICKS, 0, true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, BUFF_DURATION_TICKS, 0, true, false, true));
            player.sendMessage(Text.translatable("ability.himproveme.self_sepuku.activated").formatted(Formatting.DARK_RED), true);
        }

        return true;
    }

    /**
     * Reads the enchantment level directly from the held stack so the helper stays independent from item subclasses.
     */
    private static int getLevel(ServerPlayerEntity player, ItemStack stack) {
        RegistryEntry<Enchantment> enchantment = player.getRegistryManager()
                .getOptional(RegistryKeys.ENCHANTMENT)
                .flatMap(registry -> registry.getEntry(ModEnchantments.SELF_SEPUKU))
                .orElse(null);
        if (enchantment == null) {
            return 0;
        }

        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(enchantment);
    }

    /**
     * Persists the last world day this exact sword was used.
     */
    private static void setLastUsedDay(ItemStack stack, long day) {
        NbtComponent.set(CUSTOM_DATA, stack, compound -> {
            NbtCompound sepukuData = compound.contains(SELF_SEPUKU_DATA_KEY, NbtElement.COMPOUND_TYPE)
                    ? compound.getCompound(SELF_SEPUKU_DATA_KEY).copy()
                    : new NbtCompound();
            sepukuData.putLong(LAST_USED_DAY_KEY, day);
            compound.put(SELF_SEPUKU_DATA_KEY, sepukuData);
        });
    }

    /**
     * Returns the saved world day for the sword, or -1 when it has never been used.
     */
    private static long getLastUsedDay(ItemStack stack) {
        NbtComponent customData = stack.getOrDefault(CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound root = customData.getNbt();
        if (!root.contains(SELF_SEPUKU_DATA_KEY, NbtElement.COMPOUND_TYPE)) {
            return -1L;
        }

        NbtCompound sepukuData = root.getCompound(SELF_SEPUKU_DATA_KEY);
        return sepukuData.contains(LAST_USED_DAY_KEY, NbtElement.LONG_TYPE)
                ? sepukuData.getLong(LAST_USED_DAY_KEY)
                : -1L;
    }

    private static long getCurrentWorldDay(ServerPlayerEntity player) {
        return player.getServerWorld().getTime() / DAY_TICKS;
    }

    private static long getTicksUntilNextDay(ServerPlayerEntity player) {
        long time = player.getServerWorld().getTime();
        long nextDay = (time / DAY_TICKS + 1L) * DAY_TICKS;
        return Math.max(0L, nextDay - time);
    }

    private static String formatDuration(long ticks) {
        long totalSeconds = Math.max(0L, ticks / 20L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        if (minutes > 0L) {
            return seconds > 0L ? minutes + "m " + seconds + "s" : minutes + "m";
        }
        return seconds + "s";
    }
}
