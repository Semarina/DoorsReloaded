package de.jeff_media.doorsreloaded.utils;

import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import de.jeff_media.doorsreloaded.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/doorsreloaded/version";
    private static final String USER_AGENT = "DoorsReloaded-UpdateChecker";

    public volatile boolean updateAvailable = false;
    public volatile String latestVersionString = "";

    private final ScheduledExecutorService scheduler;

    public UpdateChecker() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DoorsReloaded-UpdateChecker");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (!ModConfig.getInstance().updates.enabled) return;

        if (ModConfig.getInstance().updates.check_on_startup) {
            scheduler.execute(this::runCheck);
        }

        double intervalHours = ModConfig.getInstance().updates.check_interval_hours;
        if (intervalHours > 0) {
            long delay = (long) (intervalHours * 60 * 60);
            scheduler.scheduleAtFixedRate(this::runCheck, delay, delay, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void runCheck() {
        try {
            String currentVersion = FabricLoader.getInstance()
                    .getModContainer(DoorsReloadedMod.MOD_ID)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");

            String response = fetchVersions();
            
            if (response != null) {
                String latestVersion = parseLatestVersion(response);
                
                if (latestVersion != null && isNewerVersion(latestVersion, currentVersion)) {
                    updateAvailable = true;
                    latestVersionString = latestVersion;
                    if (ModConfig.getInstance().updates.notify_console) {
                        DoorsReloadedMod.LOGGER.warn("A new version of DoorsReloaded is available!");
                        DoorsReloadedMod.LOGGER.warn("Current: {} | Latest: {}", currentVersion, latestVersion);
                        DoorsReloadedMod.LOGGER.warn("Download at: https://modrinth.com/plugin/doorsreloaded");
                    }
                } else if (ModConfig.getInstance().updates.notify_console && ModConfig.getInstance().debug) {
                    DoorsReloadedMod.LOGGER.info("DoorsReloaded is up to date (v{})", currentVersion);
                }
            }
        } catch (Exception e) {
            if (ModConfig.getInstance().updates.notify_console) {
                DoorsReloadedMod.LOGGER.warn("Could not check for updates: {}", e.getMessage());
            }
        }
    }

    private String fetchVersions() {
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
            if (ModConfig.getInstance().debug && ModConfig.getInstance().updates.notify_console) {
                DoorsReloadedMod.LOGGER.warn("Failed to fetch versions: {}", e.getMessage());
            }
        }
        return null;
    }

    private String parseLatestVersion(String jsonResponse) {
        Pattern pattern = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isNewerVersion(String latest, String current) {
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
            return !latest.equals(current);
        }
        return false;
    }
}
