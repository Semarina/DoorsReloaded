package de.jeff_media.doorsreloaded;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public class DoorsReloadedMod implements ModInitializer {
	public static final String MOD_ID = "doorsreloaded";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final de.jeff_media.doorsreloaded.utils.UpdateChecker UPDATE_CHECKER = new de.jeff_media.doorsreloaded.utils.UpdateChecker();

	public static void debug(String text) {
		if (de.jeff_media.doorsreloaded.config.ModConfig.getInstance().debug) {
			LOGGER.info("[DEBUG] {}", text);
		}
	}

	@Override
	public void onInitialize() {
		LOGGER.info("DoorsReloaded is initializing...");
		de.jeff_media.doorsreloaded.config.ModConfig.load();
		de.jeff_media.doorsreloaded.locale.FabricLocaleManager.reload();
		de.jeff_media.doorsreloaded.listeners.InteractionListener.register();
		de.jeff_media.doorsreloaded.scheduler.DoorScheduler.register();
		de.jeff_media.doorsreloaded.commands.DoorsReloadedCommand.register();

		UPDATE_CHECKER.start();

		// Notify OP on join if an update is available
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (de.jeff_media.doorsreloaded.config.ModConfig.getInstance().updates.enabled && de.jeff_media.doorsreloaded.config.ModConfig.getInstance().updates.notify_admins_on_join) {
				if (UPDATE_CHECKER.updateAvailable) {
					ServerPlayer player = handler.player;
					String playerName = player.getName().getString();
					boolean isOp = java.util.stream.Stream.of(server.getPlayerList().getOpNames()).anyMatch(name -> name.equalsIgnoreCase(playerName));
					if (isOp) {
						java.util.Map<String, String> placeholders = new java.util.HashMap<>();
						placeholders.put("latest", UPDATE_CHECKER.latestVersionString);
						placeholders.put("url", "https://modrinth.com/plugin/doorsreloaded");
						
						String localeCode = player.clientInformation().language();
						net.minecraft.network.chat.Component message = de.jeff_media.doorsreloaded.locale.FabricLocaleManager.getText(server.registryAccess(), localeCode, "messages.update.available_admin", placeholders);
						player.sendSystemMessage(message);
					}
				}
			}
		});

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			UPDATE_CHECKER.shutdown();
		});
	}
}
