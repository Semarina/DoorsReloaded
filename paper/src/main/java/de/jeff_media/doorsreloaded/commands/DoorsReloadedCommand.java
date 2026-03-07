package de.jeff_media.doorsreloaded.commands;

import de.jeff_media.doorsreloaded.Main;
import de.jeff_media.doorsreloaded.config.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DoorsReloadedCommand implements CommandExecutor, TabCompleter {

    private final Main main;

    public DoorsReloadedCommand() {
        main = Main.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /doorsreloaded <reload|version>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(Permissions.RELOAD)) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            main.reload();

            if (Main.getInstance().isDebug()) {
                for (String key : main.getConfig().getKeys(true)) {
                    Main.getInstance().getLogger().info(key + " -> " + main.getConfig().getString(key));
                }
            }

            sender.sendMessage(ChatColor.GREEN + "DoorsReloaded configuration has been reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(ChatColor.AQUA + "DoorsReloaded version: " + ChatColor.WHITE + main.getDescription().getVersion());
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown argument. Usage: /doorsreloaded <reload|version>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> options = new ArrayList<>(Arrays.asList("version"));

        if (sender.hasPermission(Permissions.RELOAD)) {
            options.add("reload");
        }

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], options, completions);
            Collections.sort(completions);
            return completions;
        }

        return completions;
    }
}
