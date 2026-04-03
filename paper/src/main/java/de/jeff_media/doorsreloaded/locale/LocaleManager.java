package de.jeff_media.doorsreloaded.locale;

import de.jeff_media.doorsreloaded.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/** Loads locale files and serves translated strings with placeholder support. */
public class LocaleManager {

    private static final String DEFAULT_LOCALE = "en_US";

    private final Main plugin;
    private final Logger logger;
    private final Map<String, YamlConfiguration> locales = new HashMap<>();

    public LocaleManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void reload() {
        locales.clear();
        try {
            LocaleUpdater.updateLocales(plugin);
        } catch (IOException ex) {
            logger.warning("Failed to update locale files: " + ex.getMessage());
        }

        File localeDir = new File(plugin.getDataFolder(), "locale");
        File[] files = localeDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String code = name.substring(0, name.length() - 4); // remove .yml
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                locales.put(code, yaml);
            }
        }

        if (!locales.containsKey(DEFAULT_LOCALE)) {
            logger.warning("Default locale " + DEFAULT_LOCALE + " missing; attempting to restore.");
            File defaultFile = new File(localeDir, DEFAULT_LOCALE + ".yml");
            plugin.saveResource("locale/" + DEFAULT_LOCALE + ".yml", true);
            locales.put(DEFAULT_LOCALE, YamlConfiguration.loadConfiguration(defaultFile));
        }
    }

    public String resolveLocaleFor(Player player) {
        if (player == null) return DEFAULT_LOCALE;
        String raw = player.getLocale();
        if (raw == null || raw.isBlank()) return DEFAULT_LOCALE;

        raw = raw.replace('-', '_');
        String[] parts = raw.split("_");
        if (parts.length >= 2) {
            return parts[0].toLowerCase(Locale.ROOT) + "_" + parts[1].toUpperCase(Locale.ROOT);
        }
        return raw.toLowerCase(Locale.ROOT);
    }

    public String get(CommandSender sender, String path) {
        return get(sender, path, Collections.emptyMap());
    }

    public String get(CommandSender sender, String path, Map<String, String> placeholders) {
        String localeCode = (sender instanceof Player player) ? resolveLocaleFor(player) : DEFAULT_LOCALE;
        return format(localeCode, path, placeholders);
    }

    public String getDefault(String path) {
        return getDefault(path, Collections.emptyMap());
    }

    public String getDefault(String path, Map<String, String> placeholders) {
        return format(DEFAULT_LOCALE, path, placeholders);
    }

    private String format(String localeCode, String path, Map<String, String> placeholders) {
        YamlConfiguration locale = locales.getOrDefault(localeCode, locales.get(DEFAULT_LOCALE));
        if (locale == null) {
            return path;
        }

        String message = locale.getString(path);
        if (message == null) {
            // Fallback to default locale
            if (!DEFAULT_LOCALE.equals(localeCode)) {
                YamlConfiguration def = locales.get(DEFAULT_LOCALE);
                if (def != null) {
                    message = def.getString(path);
                }
            }
        }
        if (message == null) {
            return path;
        }

        Map<String, String> mutable = new HashMap<>(placeholders);
        if (!mutable.containsKey("prefix")) {
            String prefix = locale.getString("messages.prefix", "");
            if (prefix == null) prefix = "";
            mutable.put("prefix", prefix);
        }

        for (Map.Entry<String, String> entry : mutable.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue() == null ? "" : entry.getValue();
            message = message.replace("{" + key + "}", val);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message.replaceAll("<(?i)(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white|magic|bold|strikethrough|underline|italic|reset)>", "&$1").replaceAll("<newline>", "\n").replaceAll("</(?i)(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white|magic|bold|strikethrough|underline|italic|reset)>", "&r"));
    }
}
