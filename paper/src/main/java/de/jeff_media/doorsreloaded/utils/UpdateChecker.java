package de.jeff_media.doorsreloaded.utils;

import de.jeff_media.doorsreloaded.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker implements Listener {

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/doorsreloaded/version";
    private static final String USER_AGENT = "DoorsReloaded-UpdateChecker";
    
    private static String latestVersion = null;
    private static boolean updateAvailable = false;

    public static void checkForUpdates() {
        if (!Main.getInstance().getConfig().getBoolean("updates.enabled", true) && 
            !Main.getInstance().getConfig().getBoolean("check-for-updates", true)) { // Fallback for legacy configs
            return;
        }

        Runnable task = () -> {
            try {
                String currentVersion = Main.getInstance().getDescription().getVersion();
                String response = fetchVersions();
                
                if (response != null) {
                    latestVersion = parseLatestVersion(response);
                    
                    if (latestVersion != null && isNewerVersion(latestVersion, currentVersion)) {
                        updateAvailable = true;
                        if (Main.getInstance().getConfig().getBoolean("updates.notify_console", true)) {
                            Main.getInstance().getLogger().warning("A new version of DoorsReloaded is available!");
                            Main.getInstance().getLogger().warning("Current: " + currentVersion + " | Latest: " + latestVersion);
                            Main.getInstance().getLogger().warning("Download at: https://modrinth.com/plugin/doorsreloaded");
                        }
                        
                        // Register listener for OP notifications
                        Bukkit.getPluginManager().registerEvents(new UpdateChecker(), Main.getInstance());
                    } else if (Main.getInstance().getConfig().getBoolean("updates.notify_console", true) 
                            && Main.getInstance().getConfig().getBoolean("debug", false)) {
                        Main.getInstance().getLogger().info("DoorsReloaded is up to date (v" + currentVersion + ")");
                    }
                }
            } catch (Exception e) {
                if (Main.getInstance().getConfig().getBoolean("updates.notify_console", true)) {
                    Main.getInstance().getLogger().warning("Could not check for updates: " + e.getMessage());
                }
            }
        };

        if (Main.getInstance().getConfig().getBoolean("updates.check_on_startup", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task);
        }

        double intervalHours = Main.getInstance().getConfig().getDouble("updates.check_interval_hours", 24.0);
        if (intervalHours > 0) {
            long delayTicks = (long) (intervalHours * 60 * 60 * 20); // ticks
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, delayTicks, delayTicks);
        }
    }

    private static String fetchVersions() {
        try {
            URL url = new URL(MODRINTH_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (Exception e) {
            Main.getInstance().debug("Failed to fetch versions: " + e.getMessage());
        }
        return null;
    }

    private static String parseLatestVersion(String jsonResponse) {
        // Simple JSON parsing to get first version_number
        Pattern pattern = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean isNewerVersion(String latest, String current) {
        try {
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");
            
            int length = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < length; i++) {
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("[^0-9]", "")) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("[^0-9]", "")) : 0;
                
                if (latestPart > currentPart) return true;
                if (latestPart < currentPart) return false;
            }
        } catch (NumberFormatException e) {
            // If version parsing fails, compare as strings
            return !latest.equals(current);
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (updateAvailable 
            && Main.getInstance().getConfig().getBoolean("updates.notify_admins_on_join", true)
            && player.hasPermission("doorsreloaded.notify.update")) {
            Main.getInstance().getScheduler().runLater(() -> {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("latest", latestVersion);
                map.put("url", "https://modrinth.com/plugin/doorsreloaded");
                player.sendMessage(Main.getInstance().getLocaleManager().get(player, "messages.update.available_admin", map));
            }, 40L);
        }
    }
}
