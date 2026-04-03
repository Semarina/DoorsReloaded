package de.jeff_media.doorsreloaded.locale;

import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FabricLocaleManager {

    private static final String DEFAULT_LOCALE = "en_US";
    private static final Map<String, Map<String, Object>> locales = new HashMap<>();
    private static Map<String, Object> jarDefaults = new HashMap<>();
    private static final Path LOCALE_DIR = FabricLoader.getInstance().getConfigDir().resolve("doorsreloaded/locale");

    public static void reload() {
        locales.clear();
        jarDefaults.clear();

        // Load defaults from JAR
        String[] possiblePaths = {"/locale/" + DEFAULT_LOCALE + ".yml", "locale/" + DEFAULT_LOCALE + ".yml"};
        for (String path : possiblePaths) {
            try (InputStream in = FabricLocaleManager.class.getResourceAsStream(path)) {
                if (in != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> loaded = yaml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                    if (loaded != null) {
                        jarDefaults = loaded;
                        de.jeff_media.doorsreloaded.DoorsReloadedMod.LOGGER.info("Successfully loaded JAR default locales from " + path);
                        break;
                    }
                }
            } catch (Exception e) {
                de.jeff_media.doorsreloaded.DoorsReloadedMod.LOGGER.error("Failed to load JAR default locales from " + path, e);
            }
        }

        try {
            if (LOCALE_DIR.getParent() != null && !Files.exists(LOCALE_DIR.getParent())) {
                Files.createDirectories(LOCALE_DIR.getParent());
            }
            if (!Files.exists(LOCALE_DIR)) {
                Files.createDirectories(LOCALE_DIR);
            }

            File defaultFile = LOCALE_DIR.resolve(DEFAULT_LOCALE + ".yml").toFile();
            if (!defaultFile.exists() && !jarDefaults.isEmpty()) {
                try (InputStream in = FabricLocaleManager.class.getResourceAsStream("/locale/" + DEFAULT_LOCALE + ".yml")) {
                    if (in != null) {
                        Files.copy(in, defaultFile.toPath());
                    }
                }
            }

            File[] files = LOCALE_DIR.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
            if (files != null) {
                Yaml yaml = new Yaml();
                for (File file : files) {
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                        Map<String, Object> map = yaml.load(reader);
                        if (map != null) {
                            String code = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
                            locales.put(code, map);
                        }
                    } catch (Exception e) {
                        de.jeff_media.doorsreloaded.DoorsReloadedMod.LOGGER.error("Failed to load locale file: " + file.getName(), e);
                    }
                }
            }

        } catch (Exception e) {
            de.jeff_media.doorsreloaded.DoorsReloadedMod.LOGGER.error("Failed to reload locales", e);
        }
    }

    public static String get(String localeCode, String path) {
        return get(localeCode, path, Collections.emptyMap());
    }

    public static String getDefault(String path) {
        return getDefault(path, Collections.emptyMap());
    }

    public static String getDefault(String path, Map<String, String> placeholders) {
        return get(DEFAULT_LOCALE, path, placeholders);
    }

    @SuppressWarnings("unchecked")
    private static String getRawString(Map<String, Object> map, String path) {
        if (map == null || map.isEmpty()) return null;
        String[] keys = path.split("\\.");
        Map<String, Object> current = map;
        for (int i = 0; i < keys.length; i++) {
            Object obj = current.get(keys[i]);
            if (obj == null) return null;
            if (i == keys.length - 1) {
                return obj.toString();
            }
            if (obj instanceof Map) {
                current = (Map<String, Object>) obj;
            } else {
                return null;
            }
        }
        return null;
    }

    public static String get(String localeCode, String path, Map<String, String> placeholders) {
        String code = (localeCode == null ? DEFAULT_LOCALE : localeCode).toLowerCase();
        
        // Try requested locale
        Map<String, Object> localeMap = locales.get(code);
        String message = getRawString(localeMap, path);
        
        // Try default locale if different
        if (message == null && !code.equals(DEFAULT_LOCALE.toLowerCase())) {
            message = getRawString(locales.get(DEFAULT_LOCALE.toLowerCase()), path);
        }
        
        // Try JAR defaults
        if (message == null) {
            message = getRawString(jarDefaults, path);
        }
        
        // If still null, return path
        if (message == null) {
            de.jeff_media.doorsreloaded.DoorsReloadedMod.LOGGER.warn("Missing localization key: " + path);
            return path;
        }

        Map<String, String> mutable = new HashMap<>(placeholders);
        if (!mutable.containsKey("prefix")) {
            String prefix = getRawString(localeMap, "messages.prefix");
            if (prefix == null) {
                prefix = getRawString(locales.get(DEFAULT_LOCALE.toLowerCase()), "messages.prefix");
            }
            if (prefix == null) {
                prefix = getRawString(jarDefaults, "messages.prefix");
            }
            if (prefix == null) prefix = "";
            mutable.put("prefix", prefix);
        }

        for (Map.Entry<String, String> entry : mutable.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue() == null ? "" : entry.getValue();
            message = message.replace("{" + key + "}", val);
        }
        return message;
    }

    public static net.minecraft.text.Text getText(net.minecraft.registry.RegistryWrapper.WrapperLookup registryManager, String localeCode, String path, Map<String, String> placeholders) {
        String msg = get(localeCode, path, placeholders);
        // We do not have kyori adventure available by default natively in this mod, so we use string replaces for basic formatting.
        String parsed = msg.replaceAll("<(?i)(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)>", "§$1")
                .replace("§black", "§0").replace("§dark_blue", "§1").replace("§dark_green", "§2").replace("§dark_aqua", "§3")
                .replace("§dark_red", "§4").replace("§dark_purple", "§5").replace("§gold", "§6").replace("§gray", "§7")
                .replace("§dark_gray", "§8").replace("§blue", "§9").replace("§green", "§a").replace("§aqua", "§b")
                .replace("§red", "§c").replace("§light_purple", "§d").replace("§yellow", "§e").replace("§white", "§f")
                .replace("<newline>", "\n").replaceAll("</[^>]+>", "§r");
        return net.minecraft.text.Text.literal(parsed); // The client will format standard paragraph characters natively
    }

    public static net.minecraft.text.Text getText(net.minecraft.registry.RegistryWrapper.WrapperLookup registryManager, String localeCode, String path) {
        return getText(registryManager, localeCode, path, Collections.emptyMap());
    }
}
