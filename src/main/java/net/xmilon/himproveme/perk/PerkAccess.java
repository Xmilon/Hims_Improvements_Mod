package net.xmilon.himproveme.perk;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;

public final class PerkAccess {
    public static final Identifier LAVA_SWIM_WIP = Identifier.of(HimProveMe.MOD_ID, "lava_swim_wip");
    public static final Identifier FIREPROOF = Identifier.of(HimProveMe.MOD_ID, "fireproof");
    public static final Identifier FRIENDLY_PIGLINS = Identifier.of(HimProveMe.MOD_ID, "friendly_piglins");
    public static final Identifier PIGLIN_BARTERING = Identifier.of(HimProveMe.MOD_ID, "piglin_bartering");
    public static final Identifier ENDER_STARE = Identifier.of(HimProveMe.MOD_ID, "ender_stare");
    public static final Identifier INFINITE_ENDER_PEARL = Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl");
    public static final Identifier SAFE_LEVITATION = Identifier.of(HimProveMe.MOD_ID, "safe_levitation");
    public static final Identifier MUFFLED_STEPS = Identifier.of(HimProveMe.MOD_ID, "muffled_steps");
    public static final Identifier SCULK_INVISIBILITY = Identifier.of(HimProveMe.MOD_ID, "sculk_invisibility");
    public static final Identifier LUCKY_TOTEMS = Identifier.of(HimProveMe.MOD_ID, "lucky_totems");
    public static final Identifier FRIENDLY_PILLAGERS = Identifier.of(HimProveMe.MOD_ID, "friendly_pillagers");
    public static final Identifier DOMESTICATED_RAVANGER = Identifier.of(HimProveMe.MOD_ID, "domesticated_ravanger");
    public static final Identifier JOB_APPLICATION = Identifier.of(HimProveMe.MOD_ID, "job_application");
    public static final Identifier MARKET_CONNECTIONS = Identifier.of(HimProveMe.MOD_ID, "market_connections");
    public static final Identifier TRAVELLING_TREASURES = Identifier.of(HimProveMe.MOD_ID, "travelling_treasures");
    public static final Identifier ACROBAT = Identifier.of(HimProveMe.MOD_ID, "acrobat");
    public static final Identifier WARDEN_BLEEDING = Identifier.of(HimProveMe.MOD_ID, "warden_bleeding");
    public static final Identifier WARDEN_STUNNED = Identifier.of(HimProveMe.MOD_ID, "warden_stunned");
    public static final Identifier WARDEN_FRENZY = Identifier.of(HimProveMe.MOD_ID, "warden_frenzy");

    private PerkAccess() {
    }

    public static boolean hasUnlocked(PlayerEntity player, Identifier perkId) {
        if (!(player instanceof PerkBookStateHolder holder)) {
            return false;
        }
        return holder.himproveme$getPerkBookState().getSelectedInstance().getLevel(perkId) > 0;
    }

    public static boolean hasEffect(PlayerEntity player, Identifier perkId) {
        if (!(player instanceof PerkBookStateHolder holder)) {
            return false;
        }

        PerkInstanceState instance = holder.himproveme$getPerkBookState().getSelectedInstance();
        if (instance.getLevel(perkId) <= 0) {
            return false;
        }

        PerkDefinition definition = PerkRegistry.get(perkId);
        return definition == null || !definition.toggleable() || instance.isEnabled(perkId);
    }

    public static boolean isEnabled(PlayerEntity player, Identifier perkId) {
        if (!(player instanceof PerkBookStateHolder holder)) {
            return false;
        }
        return holder.himproveme$getPerkBookState().getSelectedInstance().isEnabled(perkId);
    }

    public static boolean hasLavaSwim(PlayerEntity player) {
        return hasEffect(player, LAVA_SWIM_WIP);
    }

    public static boolean hasFireproof(PlayerEntity player) {
        return hasEffect(player, FIREPROOF);
    }

    public static boolean hasFriendlyPiglins(PlayerEntity player) {
        return hasEffect(player, FRIENDLY_PIGLINS);
    }

    public static boolean hasPiglinBartering(PlayerEntity player) {
        return hasEffect(player, PIGLIN_BARTERING);
    }

    public static boolean hasEnderStare(PlayerEntity player) {
        return hasEffect(player, ENDER_STARE);
    }

    public static boolean hasInfiniteEnderPearl(PlayerEntity player) {
        return hasEffect(player, INFINITE_ENDER_PEARL);
    }

    public static boolean hasSafeLevitation(PlayerEntity player) {
        return hasEffect(player, SAFE_LEVITATION);
    }

    public static boolean hasMuffledSteps(PlayerEntity player) {
        return hasEffect(player, MUFFLED_STEPS);
    }

    public static boolean hasSculkInvisibility(PlayerEntity player) {
        return hasEffect(player, SCULK_INVISIBILITY);
    }

    public static boolean hasLuckyTotems(PlayerEntity player) {
        return hasEffect(player, LUCKY_TOTEMS);
    }

    public static boolean hasFriendlyPillagers(PlayerEntity player) {
        return hasEffect(player, FRIENDLY_PILLAGERS);
    }

    public static boolean hasDomesticatedRavanger(PlayerEntity player) {
        return hasEffect(player, DOMESTICATED_RAVANGER);
    }

    public static boolean hasJobApplication(PlayerEntity player) {
        return hasEffect(player, JOB_APPLICATION);
    }

    public static boolean hasMarketConnections(PlayerEntity player) {
        return hasEffect(player, MARKET_CONNECTIONS);
    }

    public static boolean hasTravellingTreasures(PlayerEntity player) {
        return hasEffect(player, TRAVELLING_TREASURES);
    }

    public static boolean hasAcrobat(PlayerEntity player) {
        return hasEffect(player, ACROBAT);
    }

    public static boolean hasWardenBleeding(PlayerEntity player) {
        return hasEffect(player, WARDEN_BLEEDING);
    }

    public static boolean hasWardenStunned(PlayerEntity player) {
        return hasEffect(player, WARDEN_STUNNED);
    }

    public static boolean hasWardenFrenzy(PlayerEntity player) {
        return hasEffect(player, WARDEN_FRENZY);
    }
}
