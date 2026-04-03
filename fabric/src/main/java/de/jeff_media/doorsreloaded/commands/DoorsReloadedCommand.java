package de.jeff_media.doorsreloaded.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import de.jeff_media.doorsreloaded.config.ModConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.stream.Stream;

public class DoorsReloadedCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        var drNode = dispatcher.register(CommandManager.literal("doorsreloaded")
            .executes(context -> {
                String localeCode = "en_US";
                if (context.getSource().isExecutedByPlayer() && context.getSource().getPlayer() != null) {
                    localeCode = context.getSource().getPlayer().getClientOptions().language();
                }
                final String finalLocale = localeCode;
                try {
                    context.getSource().sendFeedback(() -> de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.doorsreloaded_help"), false);
                } catch (NoSuchMethodError e) {
                    context.getSource().sendMessage(de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.doorsreloaded_help"));
                }
                return 1;
            })
            .then(CommandManager.literal("help")
                .executes(context -> {
                    String localeCode = "en_US";
                    if (context.getSource().isExecutedByPlayer() && context.getSource().getPlayer() != null) {
                        localeCode = context.getSource().getPlayer().getClientOptions().language();
                    }
                    final String finalLocale = localeCode;
                    try {
                        context.getSource().sendFeedback(() -> de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.doorsreloaded_help"), false);
                    } catch (NoSuchMethodError e) {
                        context.getSource().sendMessage(de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.doorsreloaded_help"));
                    }
                    return 1;
                })
            )
            .then(CommandManager.literal("reload")
                .executes(context -> {
                    String localeCode = "en_US";
                    if (context.getSource().isExecutedByPlayer() && context.getSource().getPlayer() != null) {
                        localeCode = context.getSource().getPlayer().getClientOptions().language();
                        var player = context.getSource().getPlayer();
                        var server = context.getSource().getServer();
                        String playerName = player.getName().getString();

                        boolean isOp = Stream.of(server.getPlayerManager().getOpNames()).anyMatch(name -> name.equalsIgnoreCase(playerName));
                        boolean isSinglePlayer = server.isSingleplayer();

                        if (!isOp && !isSinglePlayer) {
                            final String finalLocale = localeCode;
                            try {
                                context.getSource().sendFeedback(() -> de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.no_permission"), false);
                            } catch (NoSuchMethodError e) {
                                context.getSource().sendMessage(de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.no_permission"));
                            }
                            return 0;
                        }
                    }
                    ModConfig.load();
                    de.jeff_media.doorsreloaded.locale.FabricLocaleManager.reload();
                    final String finalLocale = localeCode;
                    try {
                        // Support for 1.20+ via Supplier<Text>
                        context.getSource().sendFeedback(() -> de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.reload_success"), false);
                    } catch (NoSuchMethodError e) {
                        // Fallback for older versions if needed, though 1.21 uses Supplier
                        context.getSource().sendMessage(de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.reload_success"));
                    }
                    return 1;
                })
            )
            .then(CommandManager.literal("version")
                .executes(context -> {
                    String localeCode = "en_US";
                    if (context.getSource().isExecutedByPlayer() && context.getSource().getPlayer() != null) {
                        localeCode = context.getSource().getPlayer().getClientOptions().language();
                    }
                    final String finalLocale = localeCode;
                    String version = "Unknown";
                    var modContainer = FabricLoader.getInstance().getModContainer(DoorsReloadedMod.MOD_ID);
                    if (modContainer.isPresent()) {
                        version = modContainer.get().getMetadata().getVersion().getFriendlyString();
                    }
                    final String finalVersion = version;
                    
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("version", finalVersion);
                    
                    try {
                         context.getSource().sendFeedback(() -> de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.version", map), false);
                    } catch (NoSuchMethodError e) {
                         context.getSource().sendMessage(de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(context.getSource().getServer().getRegistryManager(), finalLocale, "messages.command.version", map));
                    }
                    return 1;
                })
            )
        );
        dispatcher.register(CommandManager.literal("dr").redirect(drNode));
    }
}
