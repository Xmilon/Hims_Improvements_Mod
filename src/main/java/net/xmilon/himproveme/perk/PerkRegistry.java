package net.xmilon.himproveme.perk;

import net.minecraft.util.Identifier;
import net.xmilon.himproveme.HimProveMe;
import net.xmilon.himproveme.util.HimColorPresets;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class PerkRegistry {
    public static final int DEFAULT_XP_LEVEL_COST = 60;
    public static final int ADVANCED_XP_LEVEL_COST = 200;
    public static final int EXTREME_XP_LEVEL_COST = 500;
    public static final int MAX_INSTANCES = 16;

    private static final Map<Identifier, PerkDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Set<GridCell> ARROW_CELLS = new HashSet<>();

    private PerkRegistry() {
    }

    public static void registerDefaults() {
        if (!DEFINITIONS.isEmpty()) {
            return;
        }

        // register(...) command set for perks:
        // - categoryKey: row/category label in the perk UI
        // - unlockFunctionKey: text shown in tooltip as "Unlocks: ..."
        // - iconItemId: item icon rendered in the perk cell
        // - unlockSoundId: main unlock/check sound event played when unlocked
        // - accentSoundId: quieter thematic sound layered on top with slight pitch variation
        // - titleColorName: named preset from HimColorPresets (project-wide reusable palette)
        //   or use the int overload for custom raw colors
        // - xpLevelCost: levels spent to unlock or upgrade this perk
        // - row/column: grid placement for this perk cell
        // - requiredPerkIds: prerequisite perks that must be unlocked first
        //
        // registerArrow(row, column) command set for path arrows:
        // - row/column: exact grid cell to render a right-arrow connector icon
        register(
                Identifier.of(HimProveMe.MOD_ID, "fireproof"),
                "perk.himproveme.fireproof",
                "perk.himproveme.fireproof.desc",
                "perk.himproveme.category.nether",
                "perk.himproveme.fireproof.function",
                Identifier.of("minecraft", "blaze_powder"),
                Identifier.of(HimProveMe.MOD_ID, "perk.fireproof_unlock"),
                Identifier.of("minecraft", "block.fire.ambient"),
                HimColorPresets.NETHER_RED,
                1,
                DEFAULT_XP_LEVEL_COST,
                0,
                0,
                List.of()
        );

        registerArrow(0, 1);

        register(
                Identifier.of(HimProveMe.MOD_ID, "friendly_piglins"),
                "perk.himproveme.friendly_piglins",
                "perk.himproveme.friendly_piglins.desc",
                "perk.himproveme.category.nether",
                "perk.himproveme.friendly_piglins.function",
                Identifier.of("minecraft", "piglin_head"),
                Identifier.of(HimProveMe.MOD_ID, "perk.friendly_piglins_unlock"),
                Identifier.of("minecraft", "entity.piglin.ambient"),
                HimColorPresets.LAVA_ORANGE,
                1,
                DEFAULT_XP_LEVEL_COST,
                0,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "fireproof"))
        );

        registerArrow(0, 3);

        register(
                Identifier.of(HimProveMe.MOD_ID, "piglin_bartering"),
                "perk.himproveme.piglin_bartering",
                "perk.himproveme.piglin_bartering.desc",
                "perk.himproveme.category.nether",
                "perk.himproveme.piglin_bartering.function",
                Identifier.of("minecraft", "gold_ingot"),
                Identifier.of(HimProveMe.MOD_ID, "perk.piglin_bartering_unlock"),
                Identifier.of("minecraft", "entity.piglin.admiring_item"),
                HimColorPresets.FIRE_GOLD,
                1,
                ADVANCED_XP_LEVEL_COST,
                0,
                4,
                List.of(Identifier.of(HimProveMe.MOD_ID, "friendly_piglins"))
        );


        // - - - - - - - - - - - END - - - - - - - - - - - - -
        register(
                Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl"),
                "perk.himproveme.infinite_ender_pearl",
                "perk.himproveme.infinite_ender_pearl.desc",
                "perk.himproveme.category.the_end",
                "perk.himproveme.infinite_ender_pearl.function",
                Identifier.of("minecraft", "ender_pearl"),
                Identifier.of(HimProveMe.MOD_ID, "perk.infinite_ender_pearl_unlock"),
                Identifier.of("minecraft", "entity.enderman.teleport"),
                HimColorPresets.ROYAL_PURPLE,
                1,
                DEFAULT_XP_LEVEL_COST,
                1,
                0,
                List.of()
        );

        registerArrow(1, 1);


        register(
                Identifier.of(HimProveMe.MOD_ID, "ender_stare"),
                "perk.himproveme.ender_stare",
                "perk.himproveme.ender_stare.desc",
                "perk.himproveme.category.the_end",
                "perk.himproveme.ender_stare.function",
                Identifier.of("minecraft", "carved_pumpkin"),
                Identifier.of(HimProveMe.MOD_ID, "perk.ender_stare_unlock"),
                Identifier.of("minecraft", "entity.enderman.ambient"),
                HimColorPresets.MAGENTA,
                1,
                DEFAULT_XP_LEVEL_COST,
                1,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl"))
        );

        registerArrow(1, 3);

        register(
                Identifier.of(HimProveMe.MOD_ID, "safe_levitation"),
                "perk.himproveme.safe_levitation",
                "perk.himproveme.safe_levitation.desc",
                "perk.himproveme.category.the_end",
                "perk.himproveme.safe_levitation.function",
                Identifier.of("minecraft", "phantom_membrane"),
                Identifier.of(HimProveMe.MOD_ID, "perk.safe_levitation_unlock"),
                Identifier.of("minecraft", "entity.shulker.shoot"),
                HimColorPresets.SKY,
                1,
                ADVANCED_XP_LEVEL_COST,
                1,
                4,
                List.of(Identifier.of(HimProveMe.MOD_ID, "ender_stare"))
        );

        // - - - - - - - - - - - THIEF - - - - - - - - - - - - -

        register(
                Identifier.of(HimProveMe.MOD_ID, "muffled_steps"),
                "perk.himproveme.muffled_steps",
                "perk.himproveme.muffled_steps.desc",
                "perk.himproveme.category.the_thief",
                "perk.himproveme.muffled_steps.function",
                Identifier.of(HimProveMe.MOD_ID, "silent"),
                Identifier.of(HimProveMe.MOD_ID, "perk.muffled_steps_unlock"),
                Identifier.of("minecraft", "block.wool.step"),
                HimColorPresets.OBSIDIAN,
                1,
                ADVANCED_XP_LEVEL_COST,
                2,
                0,
                List.of(),
                true
        );

        registerArrow(2, 1);

        register(
                Identifier.of(HimProveMe.MOD_ID, "sculk_invisibility"),
                "perk.himproveme.sculk_invisibility",
                "perk.himproveme.sculk_invisibility.desc",
                "perk.himproveme.category.the_thief",
                "perk.himproveme.sculk_invisibility.function",
                Identifier.of("minecraft", "sculk_sensor"),
                Identifier.of(HimProveMe.MOD_ID, "perk.sculk_invisibility_unlock"),
                Identifier.of("minecraft", "block.sculk_sensor.clicking_stop"),
                HimColorPresets.CYAN,
                1,
                EXTREME_XP_LEVEL_COST,
                2,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "muffled_steps"))
        );

        // - - - - - - - - - - - PILLAGER - - - - - - - - - - - - -

        register(
                Identifier.of(HimProveMe.MOD_ID, "lucky_totems"),
                "perk.himproveme.lucky_totems",
                "perk.himproveme.lucky_totems.desc",
                "perk.himproveme.category.the_pillager",
                "perk.himproveme.lucky_totems.function",
                Identifier.of("minecraft", "totem_of_undying"),
                Identifier.of(HimProveMe.MOD_ID, "perk.lucky_totems_unlock"),
                Identifier.of("minecraft", "item.totem.use"),
                HimColorPresets.SUN_YELLOW,
                1,
                ADVANCED_XP_LEVEL_COST,
                3,
                0,
                List.of()
        );

        registerArrow(3, 1);

        register(
                Identifier.of(HimProveMe.MOD_ID, "friendly_pillagers"),
                "perk.himproveme.friendly_pillagers",
                "perk.himproveme.friendly_pillagers.desc",
                "perk.himproveme.category.the_pillager",
                "perk.himproveme.friendly_pillagers.function",
                Identifier.of("minecraft", "crossbow"),
                Identifier.of(HimProveMe.MOD_ID, "perk.friendly_pillagers_unlock"),
                Identifier.of("minecraft", "entity.pillager.ambient"),
                HimColorPresets.CRIMSON,
                1,
                ADVANCED_XP_LEVEL_COST,
                3,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "lucky_totems"))
        );

        registerArrow(3, 3);

        register(
                Identifier.of(HimProveMe.MOD_ID, "domesticated_ravanger"),
                "perk.himproveme.domesticated_ravanger",
                "perk.himproveme.domesticated_ravanger.desc",
                "perk.himproveme.category.the_pillager",
                "perk.himproveme.domesticated_ravanger.function",
                Identifier.of(HimProveMe.MOD_ID, "friendly_ravanger"),
                Identifier.of(HimProveMe.MOD_ID, "perk.domesticated_ravanger_unlock"),
                Identifier.of("minecraft", "entity.ravager.ambient"),
                HimColorPresets.STEEL,
                1,
                EXTREME_XP_LEVEL_COST,
                3,
                4,
                List.of(Identifier.of(HimProveMe.MOD_ID, "friendly_pillagers"))
        );

        // - - - - - - - - - - - VILLAGER - - - - - - - - - - - - -

        register(
                Identifier.of(HimProveMe.MOD_ID, "job_application"),
                "perk.himproveme.job_application",
                "perk.himproveme.job_application.desc",
                "perk.himproveme.category.the_villager",
                "perk.himproveme.job_application.function",
                Identifier.of("minecraft", "paper"),
                Identifier.of(HimProveMe.MOD_ID, "perk.job_application_unlock"),
                Identifier.of("minecraft", "entity.villager.work_cartographer"),
                HimColorPresets.EMERALD,
                1,
                DEFAULT_XP_LEVEL_COST,
                4,
                0,
                List.of()
        );

        registerArrow(4, 1);

        register(
                Identifier.of(HimProveMe.MOD_ID, "market_connections"),
                "perk.himproveme.market_connections",
                "perk.himproveme.market_connections.desc",
                "perk.himproveme.category.the_villager",
                "perk.himproveme.market_connections.function",
                Identifier.of("minecraft", "emerald"),
                Identifier.of(HimProveMe.MOD_ID, "perk.market_connections_unlock"),
                Identifier.of("minecraft", "entity.villager.yes"),
                HimColorPresets.SUN_YELLOW,
                1,
                ADVANCED_XP_LEVEL_COST,
                4,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "job_application"))
        );

        registerArrow(4, 3);

        register(
                Identifier.of(HimProveMe.MOD_ID, "travelling_treasures"),
                "perk.himproveme.travelling_treasures",
                "perk.himproveme.travelling_treasures.desc",
                "perk.himproveme.category.the_villager",
                "perk.himproveme.travelling_treasures.function",
                Identifier.of("minecraft", "wandering_trader_spawn_egg"),
                Identifier.of(HimProveMe.MOD_ID, "perk.travelling_treasures_unlock"),
                Identifier.of("minecraft", "entity.wandering_trader.ambient"),
                HimColorPresets.LIME,
                1,
                EXTREME_XP_LEVEL_COST,
                4,
                4,
                List.of(Identifier.of(HimProveMe.MOD_ID, "market_connections"))
        );

        // - - - - - - - - - - - ACROBAT - - - - - - - - - - - - -

        register(
                Identifier.of(HimProveMe.MOD_ID, "acrobat"),
                "perk.himproveme.acrobat",
                "perk.himproveme.acrobat.desc",
                "perk.himproveme.category.acrobat",
                "perk.himproveme.acrobat.function",
                Identifier.of("minecraft", "feather"),
                Identifier.of("minecraft", "entity.player.attack.sweep"),
                Identifier.of("minecraft", "entity.phantom.flap"),
                HimColorPresets.SKY,
                1,
                ADVANCED_XP_LEVEL_COST,
                5,
                0,
                List.of(),
                true
        );

        // - - - - - - - - - - - WARDEN - - - - - - - - - - - - -

        register(
                Identifier.of(HimProveMe.MOD_ID, "warden_bleeding"),
                "perk.himproveme.warden_bleeding",
                "perk.himproveme.warden_bleeding.desc",
                "perk.himproveme.category.the_warden",
                "perk.himproveme.warden_bleeding.function",
                Identifier.of(HimProveMe.MOD_ID, "warden_bleeding"),
                Identifier.of("minecraft", "block.sculk_shrieker.shriek"),
                Identifier.of("minecraft", "entity.warden.heartbeat"),
                0xFF8E2B2F,
                1,
                EXTREME_XP_LEVEL_COST,
                6,
                0,
                List.of(),
                true
        );

        registerArrow(6, 1);

        register(
                Identifier.of(HimProveMe.MOD_ID, "warden_stunned"),
                "perk.himproveme.warden_stunned",
                "perk.himproveme.warden_stunned.desc",
                "perk.himproveme.category.the_warden",
                "perk.himproveme.warden_stunned.function",
                Identifier.of(HimProveMe.MOD_ID, "warden_stunned"),
                Identifier.of("minecraft", "entity.warden.sonic_boom"),
                Identifier.of("minecraft", "entity.warden.nearby_close"),
                0xFFB6B46B,
                1,
                EXTREME_XP_LEVEL_COST,
                6,
                2,
                List.of(),
                true
        );

        registerArrow(6, 3);

        register(
                Identifier.of(HimProveMe.MOD_ID, "warden_frenzy"),
                "perk.himproveme.warden_frenzy",
                "perk.himproveme.warden_frenzy.desc",
                "perk.himproveme.category.the_warden",
                "perk.himproveme.warden_frenzy.function",
                Identifier.of(HimProveMe.MOD_ID, "warden_frenzy"),
                Identifier.of("minecraft", "entity.warden.angry"),
                Identifier.of("minecraft", "entity.creeper.primed"),
                0xFFB13A31,
                1,
                EXTREME_XP_LEVEL_COST,
                6,
                4,
                List.of(),
                true
        );


    }

    public static Collection<PerkDefinition> values() {
        return DEFINITIONS.values();
    }

    public static PerkDefinition get(Identifier id) {
        return DEFINITIONS.get(id);
    }

    public static List<PerkDefinition> valuesOrderedByGrid() {
        return DEFINITIONS.values().stream()
                .sorted(Comparator.comparingInt(PerkDefinition::row).thenComparingInt(PerkDefinition::column))
                .collect(Collectors.toList());
    }

    public static boolean isArrowCell(int row, int column) {
        return ARROW_CELLS.contains(new GridCell(row, column));
    }

    public static int getMaxColumnForRow(int row) {
        int max = -1;
        for (PerkDefinition definition : DEFINITIONS.values()) {
            if (definition.row() == row && definition.column() > max) {
                max = definition.column();
            }
        }
        for (GridCell cell : ARROW_CELLS) {
            if (cell.row() == row && cell.column() > max) {
                max = cell.column();
            }
        }
        return Math.max(0, max);
    }

    private static void register(
            Identifier id,
            String nameKey,
            String descriptionKey,
            String categoryKey,
            String unlockFunctionKey,
            Identifier iconItemId,
            Identifier unlockSoundId,
            Identifier accentSoundId,
            String titleColorName,
            int maxLevel,
            int xpLevelCost,
            int row,
            int column,
            List<Identifier> requiredPerkIds
    ) {
        register(
                id,
                nameKey,
                descriptionKey,
                categoryKey,
                unlockFunctionKey,
                iconItemId,
                unlockSoundId,
                accentSoundId,
                titleColorName,
                maxLevel,
                xpLevelCost,
                row,
                column,
                requiredPerkIds,
                false
        );
    }

    private static void register(
            Identifier id,
            String nameKey,
            String descriptionKey,
            String categoryKey,
            String unlockFunctionKey,
            Identifier iconItemId,
            Identifier unlockSoundId,
            Identifier accentSoundId,
            String titleColorName,
            int maxLevel,
            int xpLevelCost,
            int row,
            int column,
            List<Identifier> requiredPerkIds,
            boolean toggleable
    ) {
        register(
                id,
                nameKey,
                descriptionKey,
                categoryKey,
                unlockFunctionKey,
                iconItemId,
                unlockSoundId,
                accentSoundId,
                HimColorPresets.get(titleColorName),
                maxLevel,
                xpLevelCost,
                row,
                column,
                requiredPerkIds,
                toggleable
        );
    }

    private static void register(
            Identifier id,
            String nameKey,
            String descriptionKey,
            String categoryKey,
            String unlockFunctionKey,
            Identifier iconItemId,
            Identifier unlockSoundId,
            Identifier accentSoundId,
            int titleColor,
            int maxLevel,
            int xpLevelCost,
            int row,
            int column,
            List<Identifier> requiredPerkIds
    ) {
        register(
                id,
                nameKey,
                descriptionKey,
                categoryKey,
                unlockFunctionKey,
                iconItemId,
                unlockSoundId,
                accentSoundId,
                titleColor,
                maxLevel,
                xpLevelCost,
                row,
                column,
                requiredPerkIds,
                false
        );
    }

    private static void register(
            Identifier id,
            String nameKey,
            String descriptionKey,
            String categoryKey,
            String unlockFunctionKey,
            Identifier iconItemId,
            Identifier unlockSoundId,
            Identifier accentSoundId,
            int titleColor,
            int maxLevel,
            int xpLevelCost,
            int row,
            int column,
            List<Identifier> requiredPerkIds,
            boolean toggleable
    ) {
        DEFINITIONS.put(
                id,
                new PerkDefinition(
                        id,
                        nameKey,
                        descriptionKey,
                        categoryKey,
                        unlockFunctionKey,
                        iconItemId,
                        unlockSoundId,
                        accentSoundId,
                        titleColor,
                        maxLevel,
                        xpLevelCost,
                        row,
                        column,
                        requiredPerkIds,
                        toggleable
                )
        );
    }

    public static void registerArrow(int row, int column) {
        ARROW_CELLS.add(new GridCell(row, column));
    }

    private record GridCell(int row, int column) {
    }
}
