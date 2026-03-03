package net.xmilon.himproveme.perk;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public final class PerkAccess {
    public static final Identifier LAVA_SWIM_WIP = Identifier.of(HimProveMe.MOD_ID, "lava_swim_wip");
    public static final Identifier ENDER_STARE = Identifier.of(HimProveMe.MOD_ID, "ender_stare");
    public static final Identifier INFINITE_ENDER_PEARL = Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl");

    private PerkAccess() {
    }

    public static boolean hasUnlocked(PlayerEntity player, Identifier perkId) {
        if (!(player instanceof PerkBookStateHolder holder)) {
            return false;
        }
        return holder.himproveme$getPerkBookState().getSelectedInstance().getLevel(perkId) > 0;
    }

    public static boolean hasLavaSwim(PlayerEntity player) {
        return hasUnlocked(player, LAVA_SWIM_WIP);
    }

    public static boolean hasEnderStare(PlayerEntity player) {
        return hasUnlocked(player, ENDER_STARE);
    }

    public static boolean hasInfiniteEnderPearl(PlayerEntity player) {
        return hasUnlocked(player, INFINITE_ENDER_PEARL);
    }
}
