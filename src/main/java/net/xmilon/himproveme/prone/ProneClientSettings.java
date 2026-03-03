package net.xmilon.himproveme.prone;

import net.fabricmc.loader.api.FabricLoader;
import net.xmilon.himproveme.HimProveMe;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ProneClientSettings {
    public enum Mode {
        HOLD,
        TOGGLE
    }

    private static final String MODE_KEY = "prone_mode";
    private static final Path SETTINGS_PATH = FabricLoader.getInstance().getConfigDir().resolve(HimProveMe.MOD_ID + "-client.properties");
    private static boolean loaded;
    private static Mode mode = Mode.HOLD;

    private ProneClientSettings() {
    }

    public static Mode getMode() {
        ensureLoaded();
        return mode;
    }

    public static void setMode(Mode newMode) {
        ensureLoaded();
        mode = newMode == null ? Mode.HOLD : newMode;
        save();
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        load();
    }

    private static void load() {
        if (!Files.exists(SETTINGS_PATH)) {
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(SETTINGS_PATH)) {
            properties.load(reader);
            String raw = properties.getProperty(MODE_KEY, Mode.HOLD.name());
            mode = "TOGGLE".equalsIgnoreCase(raw) ? Mode.TOGGLE : Mode.HOLD;
        } catch (IOException ignored) {
            mode = Mode.HOLD;
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(MODE_KEY, mode.name());
        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(SETTINGS_PATH)) {
                properties.store(writer, "HimProveMe client settings");
            }
        } catch (IOException ignored) {
        }
    }
}
