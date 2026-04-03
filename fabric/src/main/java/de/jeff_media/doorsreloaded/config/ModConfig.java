package de.jeff_media.doorsreloaded.config;

import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class ModConfig {

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("doorsreloaded/doorsreloaded.yml").toFile();
    private static ModConfig INSTANCE;

    // Config fields
    public int config_version = 2;
    public boolean allow_doubledoors = true;
    public boolean check_for_redstone = true;
    public boolean allow_opening_irondoors_with_hands = false;
    public boolean allow_opening_irontrapdoors_with_hands = false;
    public long autoclose = 0;
    public boolean allow_knocking_doors = true;
    public boolean allow_knocking_trapdoors = false;
    public boolean allow_knocking_gates = false;
    public boolean knocking_requires_empty_hand = false;
    public boolean knocking_requires_shift = false;

    public String sound_knock_door_iron = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_door_copper = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_door_wood = "minecraft:item.shield.block";

    public String sound_knock_trapdoor_iron = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_trapdoor_copper = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_trapdoor_wood = "minecraft:item.shield.block";

    public String sound_knock_gate_iron = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_gate_copper = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_gate_wood = "minecraft:item.shield.block";
    public double sound_knock_volume = 1.0;
    public double sound_knock_pitch = 1.0;
    public String sound_knock_category = "BLOCKS"; // Added category
    public boolean debug = false;
    public UpdateConfig updates = new UpdateConfig();

    public static class UpdateConfig {
        public boolean enabled = true;
        public boolean check_on_startup = true;
        public double check_interval_hours = 24.0;
        public boolean notify_console = true;
        public boolean notify_admins_on_join = true;
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        INSTANCE = new ModConfig();
        if (CONFIG_FILE.getParentFile() != null && !CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        
        // Auto-migrate from old `.properties` file if it exists
        File oldPropsFile = FabricLoader.getInstance().getConfigDir().resolve("doorsreloaded/doorsreloaded.properties").toFile();
        if (oldPropsFile.exists() && !CONFIG_FILE.exists()) {
            try { Files.move(oldPropsFile.toPath(), CONFIG_FILE.toPath()); } catch (Exception ignored) {}
        }
        File oldestPropsFile = FabricLoader.getInstance().getConfigDir().resolve("doorsreloaded.properties").toFile();
        if (oldestPropsFile.exists() && !CONFIG_FILE.exists()) {
            try { Files.move(oldestPropsFile.toPath(), CONFIG_FILE.toPath()); } catch (Exception ignored) {}
        }

        if (CONFIG_FILE.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
                Yaml yaml = new Yaml();
                Map<String, Object> map = yaml.load(reader);
                
                // Fallback for reading properties if snakeyaml fails (during migration from .properties to .yml)
                if (map == null) {
                    try (InputStreamReader propsReader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
                        java.util.Properties props = new java.util.Properties();
                        props.load(propsReader);
                        map = new java.util.HashMap<>();
                        for (String name : props.stringPropertyNames()) {
                            map.put(name, props.getProperty(name));
                        }
                    }
                }

                if (map != null) {
                    INSTANCE.config_version = getInt(map, "config-version", 2);
                    INSTANCE.allow_doubledoors = getBoolean(map, "allow-doubledoors", true);
                    INSTANCE.check_for_redstone = getBoolean(map, "check-for-redstone", true);
                    
                    boolean oldOpeningIronDoors = getBoolean(map, "allow-opening-irondoors-with-hands", false);
                    INSTANCE.allow_opening_irondoors_with_hands = getBoolean(map, "allow-opening-irondoors-with-hands", oldOpeningIronDoors);
                    INSTANCE.allow_opening_irontrapdoors_with_hands = getBoolean(map, "allow-opening-irontrapdoors-with-hands", oldOpeningIronDoors);
                    
                    INSTANCE.autoclose = getLong(map, "autoclose", 0L);
                    
                    // Fallback for older configs
                    boolean oldAllowKnocking = getBoolean(map, "allow-knocking", true);
                    INSTANCE.allow_knocking_doors = getBoolean(map, "allow-knocking-doors", oldAllowKnocking);
                    INSTANCE.allow_knocking_trapdoors = getBoolean(map, "allow-knocking-trapdoors", false);
                    INSTANCE.allow_knocking_gates = getBoolean(map, "allow-knocking-gates", false);
                    INSTANCE.knocking_requires_empty_hand = getBoolean(map, "knocking-requires-empty-hand", false);
                    INSTANCE.knocking_requires_shift = getBoolean(map, "knocking-requires-shift", false);

                    String oldIron = getString(map, "sound-knock-iron");
                    String oldCopper = getString(map, "sound-knock-copper");
                    String oldWood = getString(map, "sound-knock-wood");

                    INSTANCE.sound_knock_door_iron = getString(map, "sound-knock-door-iron", oldIron != null ? oldIron : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_door_copper = getString(map, "sound-knock-door-copper", oldCopper != null ? oldCopper : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_door_wood = getString(map, "sound-knock-door-wood", oldWood != null ? oldWood : "minecraft:item.shield.block");

                    INSTANCE.sound_knock_trapdoor_iron = getString(map, "sound-knock-trapdoor-iron", oldIron != null ? oldIron : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_trapdoor_copper = getString(map, "sound-knock-trapdoor-copper", oldCopper != null ? oldCopper : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_trapdoor_wood = getString(map, "sound-knock-trapdoor-wood", oldWood != null ? oldWood : "minecraft:item.shield.block");

                    INSTANCE.sound_knock_gate_iron = getString(map, "sound-knock-gate-iron", oldIron != null ? oldIron : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_gate_copper = getString(map, "sound-knock-gate-copper", oldCopper != null ? oldCopper : "minecraft:entity.zombie.attack_iron_door");
                    INSTANCE.sound_knock_gate_wood = getString(map, "sound-knock-gate-wood", oldWood != null ? oldWood : "minecraft:item.shield.block");
                    INSTANCE.sound_knock_volume = getDouble(map, "sound-knock-volume", 1.0);
                    INSTANCE.sound_knock_pitch = getDouble(map, "sound-knock-pitch", 1.0);
                    INSTANCE.debug = getBoolean(map, "debug", false);

                    if (map.containsKey("updates")) {
                        Map<String, Object> updates = (Map<String, Object>) map.get("updates");
                        if (updates.containsKey("enabled")) INSTANCE.updates.enabled = getBoolean(updates, "enabled", true);
                        if (updates.containsKey("check_on_startup")) INSTANCE.updates.check_on_startup = getBoolean(updates, "check_on_startup", true);
                        if (updates.containsKey("check_interval_hours")) INSTANCE.updates.check_interval_hours = getDouble(updates, "check_interval_hours", 24.0);
                        if (updates.containsKey("notify_console")) INSTANCE.updates.notify_console = getBoolean(updates, "notify_console", true);
                        if (updates.containsKey("notify_admins_on_join")) INSTANCE.updates.notify_admins_on_join = getBoolean(updates, "notify_admins_on_join", true);
                    } else if (map.containsKey("check-for-updates")) {
                        // Fallback from old config
                        INSTANCE.updates.enabled = getBoolean(map, "check-for-updates", true);
                        INSTANCE.updates.check_on_startup = true;
                        INSTANCE.updates.notify_admins_on_join = true;
                        INSTANCE.updates.notify_console = true;
                    }

                    if (INSTANCE.config_version < 2) {
                        DoorsReloadedMod.LOGGER.info("Found outdated config (version " + INSTANCE.config_version + "), updating to version 2...");
                        INSTANCE.config_version = 2;
                        save();
                        DoorsReloadedMod.LOGGER.info("Config updated successfully.");
                    }
                }
            } catch (Exception e) {
                DoorsReloadedMod.LOGGER.error("Failed to load config", e);
            }
        }
        
        save(); // Save defaults and rewrite properties to yaml format
    }

    public static void save() {
        // Manually writing to preserve comments and structure
        StringBuilder sb = new StringBuilder();

        sb.append("###########################\n");
        sb.append("#       DoorsReloaded     #\n");
        sb.append("###########################\n\n");

        sb.append("config-version: ").append(INSTANCE.config_version).append("\n\n");

        sb.append("# Original author: mfnalex / JEFF Media GbR\n");
        sb.append("# Community continuation by Semarina.\n\n");

        sb.append("###########################\n");
        sb.append("#       Double doors      #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can open-close double doors with right-click.\n");
        sb.append("# They need the permission \"doorsreloaded.doubledoors\" which is given to all players by default\n");
        sb.append("allow-doubledoors: ").append(INSTANCE.allow_doubledoors).append("\n\n");

        sb.append("# Also open/close both doors when one door gets powered by redstone.\n");
        sb.append("# Note: if both doors are powered and only signal gets cut, only one door will close (like in vanilla)\n");
        sb.append("check-for-redstone: ").append(INSTANCE.check_for_redstone).append("\n\n");

        sb.append("###########################\n");
        sb.append("#        Iron Doors       #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can open-close iron doors with right-click.\n");
        sb.append("# They need the permission \"doorsreloaded.irondoors\".\n");
        sb.append("allow-opening-irondoors-with-hands: ").append(INSTANCE.allow_opening_irondoors_with_hands).append("\n\n");

        sb.append("# When true, players can open-close iron trapdoors with right-click.\n");
        sb.append("# They need the permission \"doorsreloaded.irondoors\".\n");
        sb.append("allow-opening-irontrapdoors-with-hands: ").append(INSTANCE.allow_opening_irontrapdoors_with_hands).append("\n\n");

        sb.append("# Automatically closes iron doors and trapdoors after the given amount of seconds (0 to disable).\n");
        sb.append("autoclose: ").append(INSTANCE.autoclose).append("\n\n");

        sb.append("###########################\n");
        sb.append("#         Knocking        #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can knock on doors using left-click.\n");
        sb.append("# This only works for players in survival and adventure mode.\n");
        sb.append("# They need the permission \"doorsreloaded.knock\" which is given to all players by default\n");
        sb.append("allow-knocking-doors: ").append(INSTANCE.allow_knocking_doors).append("\n\n");

        sb.append("# When true, players can knock to trapdoors like normal doors.\n");
        sb.append("# Players still need the permission \"doorsreloaded.knock\" which is given to all players by default\n");
        sb.append("# If allow-knocking-doors is false, this feature won't work\n");
        sb.append("allow-knocking-trapdoors: ").append(INSTANCE.allow_knocking_trapdoors).append("\n\n");

        sb.append("# When true, players can knock to fence gates like normal doors.\n");
        sb.append("# Players still need the permission \"doorsreloaded.knock\" which is given to all players by default\n");
        sb.append("# If allow-knocking-doors is false, this feature won't work\n");
        sb.append("allow-knocking-gates: ").append(INSTANCE.allow_knocking_gates).append("\n\n");

        sb.append("# When true, players can only knock when their hand is empty.\n");
        sb.append("knocking-requires-empty-hand: ").append(INSTANCE.knocking_requires_empty_hand).append("\n\n");

        sb.append("# When true, players can only knock while sneaking\n");
        sb.append("knocking-requires-shift: ").append(INSTANCE.knocking_requires_shift).append("\n\n");

        sb.append("###########################\n");
        sb.append("#     Knocking Sounds     #\n");
        sb.append("###########################\n\n");

        sb.append("# Settings for the knocking sound\n");
        sb.append("# See here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\n");
        sb.append("sound-knock-door-iron: \"").append(INSTANCE.sound_knock_door_iron).append("\"\n");
        sb.append("sound-knock-door-copper: \"").append(INSTANCE.sound_knock_door_copper).append("\"\n");
        sb.append("sound-knock-door-wood: \"").append(INSTANCE.sound_knock_door_wood).append("\"\n\n");

        sb.append("sound-knock-trapdoor-iron: \"").append(INSTANCE.sound_knock_trapdoor_iron).append("\"\n");
        sb.append("sound-knock-trapdoor-copper: \"").append(INSTANCE.sound_knock_trapdoor_copper).append("\"\n");
        sb.append("sound-knock-trapdoor-wood: \"").append(INSTANCE.sound_knock_trapdoor_wood).append("\"\n\n");

        sb.append("sound-knock-gate-iron: \"").append(INSTANCE.sound_knock_gate_iron).append("\"\n");
        sb.append("sound-knock-gate-copper: \"").append(INSTANCE.sound_knock_gate_copper).append("\"\n");
        sb.append("sound-knock-gate-wood: \"").append(INSTANCE.sound_knock_gate_wood).append("\"\n");
        sb.append("# A volume of 1.0 means 16 blocks, 2.0 means 32 blocks, etc.\n");
        sb.append("sound-knock-volume: ").append(INSTANCE.sound_knock_volume).append("\n");
        sb.append("sound-knock-pitch: ").append(INSTANCE.sound_knock_pitch).append("\n");
        sb.append("# See here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/SoundCategory.html\n");
        sb.append("sound-knock-category: \"").append(INSTANCE.sound_knock_category).append("\"\n\n");

        sb.append("###########################\n");
        sb.append("#           Misc          #\n");
        sb.append("###########################\n\n");

        sb.append("# Debug mode\n");
        sb.append("debug: ").append(INSTANCE.debug).append("\n\n");

        sb.append("###########################\n");
        sb.append("#         Updates         #\n");
        sb.append("###########################\n\n");

        sb.append("# Optional Modrinth update checks for DoorsReloaded.\n");
        sb.append("# Set enabled to false to disable HTTP calls entirely.\n");
        sb.append("updates:\n");
        sb.append("  enabled: ").append(INSTANCE.updates.enabled).append("\n");
        sb.append("  check_on_startup: ").append(INSTANCE.updates.check_on_startup).append("\n");
        sb.append("  # Interval in hours to check for new updates (0 or negative = startup-only)\n");
        sb.append("  check_interval_hours: ").append(INSTANCE.updates.check_interval_hours).append("\n");
        sb.append("  notify_console: ").append(INSTANCE.updates.notify_console).append("\n");
        sb.append("  # Notifies administrators (OPs) when they log into the server\n");
        sb.append("  notify_admins_on_join: ").append(INSTANCE.updates.notify_admins_on_join).append("\n");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            DoorsReloadedMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private static String getString(Map<String, Object> map, String key, String def) {
        String val = getString(map, key);
        return val != null ? val : def;
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object val = map.get(key);
        if (val == null) return def;
        if (val instanceof Boolean) return (Boolean) val;
        return Boolean.parseBoolean(val.toString());
    }
    
    private static int getInt(Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val == null) return def;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return def; }
    }

    private static long getLong(Map<String, Object> map, String key, long def) {
        Object val = map.get(key);
        if (val == null) return def;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return def; }
    }

    private static double getDouble(Map<String, Object> map, String key, double def) {
        Object val = map.get(key);
        if (val == null) return def;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) { return def; }
    }
}
