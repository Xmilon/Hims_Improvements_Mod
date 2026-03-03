package net.xmilon.himproveme.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HimColorPresets {
    public static final String WHITE = "white";
    public static final String LAVA_ORANGE = "lava_orange";
    public static final String FIRE_GOLD = "fire_gold";
    public static final String NETHER_RED = "nether_red";
    public static final String EMERALD = "emerald";
    public static final String STEEL = "steel";
    public static final String VIOLET = "violet";
    public static final String BLUE = "blue";
    public static final String CYAN = "cyan";
    public static final String MAGENTA = "magenta";
    public static final String CRIMSON = "crimson";
    public static final String ROYAL_PURPLE = "royal_purple";
    public static final String LIME = "lime";
    public static final String SKY = "sky";
    public static final String SUN_YELLOW = "sun_yellow";
    public static final String OBSIDIAN = "obsidian";
    public static final String SILVER = "silver";

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final Map<String, Integer> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put(WHITE, 0xFFFFFFFF);
        PRESETS.put(LAVA_ORANGE, 0xFFFFA83A);
        PRESETS.put(FIRE_GOLD, 0xFFFFC766);
        PRESETS.put(NETHER_RED, 0xFFE45A5A);
        PRESETS.put(EMERALD, 0xFF58D68D);
        PRESETS.put(STEEL, 0xFFBFC9CA);
        PRESETS.put(VIOLET, 0xFF8F00FF);
        PRESETS.put(BLUE, 0xFF4A90E2);
        PRESETS.put(CYAN, 0xFF00BCD4);
        PRESETS.put(MAGENTA, 0xFFE040FB);
        PRESETS.put(CRIMSON, 0xFFDC143C);
        PRESETS.put(ROYAL_PURPLE, 0xFF6A0DAD);
        PRESETS.put(LIME, 0xFF7ED957);
        PRESETS.put(SKY, 0xFF6EC6FF);
        PRESETS.put(SUN_YELLOW, 0xFFFFE066);
        PRESETS.put(OBSIDIAN, 0xFF2D2238);
        PRESETS.put(SILVER, 0xFFDADFE1);
    }

    private HimColorPresets() {
    }

    public static int get(String presetName) {
        if (presetName == null) {
            return DEFAULT_COLOR;
        }
        return PRESETS.getOrDefault(presetName.toLowerCase(), DEFAULT_COLOR);
    }

    public static Map<String, Integer> all() {
        return Collections.unmodifiableMap(PRESETS);
    }
}
