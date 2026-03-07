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

public class DoorsReloadedCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("doorsreloaded")
            .then(CommandManager.literal("reload")
                .executes(context -> {
                    ModConfig.load();
                    try {
                        // Support for 1.20+ via Supplier<Text>
                        context.getSource().sendFeedback(() -> Text.literal("DoorsReloaded configuration has been reloaded.").formatted(Formatting.GREEN), false);
                    } catch (NoSuchMethodError e) {
                        // Fallback for older versions if needed, though 1.21 uses Supplier
                        context.getSource().sendMessage(Text.literal("DoorsReloaded configuration has been reloaded.").formatted(Formatting.GREEN));
                    }
                    return 1;
                })
            )
            .then(CommandManager.literal("version")
                .executes(context -> {
                    String version = "Unknown";
                    var modContainer = FabricLoader.getInstance().getModContainer(DoorsReloadedMod.MOD_ID);
                    if (modContainer.isPresent()) {
                        version = modContainer.get().getMetadata().getVersion().getFriendlyString();
                    }
                    final String finalVersion = version;
                    try {
                         context.getSource().sendFeedback(() -> Text.literal("DoorsReloaded version: ").formatted(Formatting.AQUA).append(Text.literal(finalVersion).formatted(Formatting.WHITE)), false);
                    } catch (NoSuchMethodError e) {
                         context.getSource().sendMessage(Text.literal("DoorsReloaded version: ").formatted(Formatting.AQUA).append(Text.literal(finalVersion).formatted(Formatting.WHITE)));
                    }
                    return 1;
                })
            )
        );
    }
}
