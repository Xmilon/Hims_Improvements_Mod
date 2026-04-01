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
    public static final int XP_LEVEL_COST_PER_UPGRADE = 20;
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
        // - unlockSoundId: sound event played when unlocked
        // - titleColorName: named preset from HimColorPresets (project-wide reusable palette)
        //   or use the int overload for custom raw colors
        // - row/column: grid placement for this perk cell
        // - requiredPerkIds: prerequisite perks that must be unlocked first
        //
        // registerArrow(row, column) command set for path arrows:
        // - row/column: exact grid cell to render a right-arrow connector icon
        register(
                Identifier.of(HimProveMe.MOD_ID, "lava_swim"),
                "perk.himproveme.lava_swim",
                "perk.himproveme.lava_swim.desc",
                "perk.himproveme.category.nether",
                "perk.himproveme.lava_swim.function",
                Identifier.of("minecraft", "lava_bucket"),
                Identifier.of(HimProveMe.MOD_ID, "perk.lava_swim_unlock"),
                HimColorPresets.LAVA_ORANGE,
                1,
                0,
                0,
                List.of()
        );

        registerArrow(0, 1);

        // Registers Fireproof with a dependency on Lava Swim WIP in the same row.
        register(
                Identifier.of(HimProveMe.MOD_ID, "fireproof"),
                "perk.himproveme.fireproof",
                "perk.himproveme.fireproof.desc",
                "perk.himproveme.category.nether",
                "perk.himproveme.fireproof.function",
                Identifier.of("minecraft", "blaze_powder"),
                Identifier.of(HimProveMe.MOD_ID, "perk.fireproof_unlock"),
                HimColorPresets.FIRE_GOLD,
                1,
                0,
                2,
                List.of(Identifier.of(HimProveMe.MOD_ID, "lava_swim"))
        );

        registerArrow(0, 3);


        // - - - - - - - - - - - END - - - - - - - - - - - - -
        register(
                Identifier.of(HimProveMe.MOD_ID, "infinite_ender_pearl"),
                "perk.himproveme.infinite_ender_pearl",
                "perk.himproveme.infinite_ender_pearl.desc",
                "perk.himproveme.category.the_end",
                "perk.himproveme.infinite_ender_pearl.function",
                Identifier.of("minecraft", "ender_pearl"),
                Identifier.of(HimProveMe.MOD_ID, "perk.infinite_ender_pearl_unlock"),
                HimColorPresets.VIOLET,
                1,
                1,
                0,
                List.of()
        );

        registerArrow(1, 1);

        // Registers Fireproof with a dependency on Lava Swim WIP in the same row.
        register(
                Identifier.of(HimProveMe.MOD_ID, "ender_stare"),
                "perk.himproveme.ender_stare",
                "perk.himproveme.ender_stare.desc",
                "perk.himproveme.category.the_end",
                "perk.himproveme.ender_stare.function",
                Identifier.of("minecraft", "carved_pumpkin"),
                Identifier.of(HimProveMe.MOD_ID, "perk.ender_stare_unlock"),
                HimColorPresets.VIOLET,
                1,
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
                HimColorPresets.VIOLET,
                1,
                1,
                4,
                List.of(Identifier.of(HimProveMe.MOD_ID, "ender_stare"))
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
            String titleColorName,
            int maxLevel,
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
                HimColorPresets.get(titleColorName),
                maxLevel,
                row,
                column,
                requiredPerkIds
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
            int titleColor,
            int maxLevel,
            int row,
            int column,
            List<Identifier> requiredPerkIds
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
                        titleColor,
                        maxLevel,
                        row,
                        column,
                        requiredPerkIds
                )
        );
    }

    public static void registerArrow(int row, int column) {
        ARROW_CELLS.add(new GridCell(row, column));
    }

    private record GridCell(int row, int column) {
    }
}
