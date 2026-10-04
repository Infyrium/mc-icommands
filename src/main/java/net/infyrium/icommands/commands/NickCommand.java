package net.infyrium.icommands.commands;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class NickCommand implements CommandExecutor {

    private static final Pattern COLOR_CODES = Pattern.compile("&[0-9a-fk-orA-FK-OR]");

    private static final Pattern ALLOWED_NICK = Pattern.compile("^[a-zA-Z_0-9]+$");

    private static final int MAX_NICK_LENGTH = 16;

    private final iCommandsMain plugin;

    public NickCommand(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        String nick;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /" + label + " <player> <nickname|off>");
                return true;
            }
            if (plugin.getConfig().getBoolean("settings.requirePermission.nick", true)
                    && !sender.hasPermission("icommands.nick")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }
            target = player;
            nick = args[0];
        } else if (args.length == 2) {
            if (!sender.hasPermission("icommands.nick.others")) {
                sender.sendMessage("You don't have permission to use this command on other players.");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null || (sender instanceof Player player && !player.canSee(target))) {
                sender.sendMessage("Player '" + args[0] + "' is not online.");
                return true;
            }
            nick = args[1];
        } else {
            sender.sendMessage("Usage: /" + label + " [player] <nickname|off>");
            return true;
        }

        if (nick.equalsIgnoreCase("off")) {
            plugin.setNick(target, null);
            target.sendMessage("You no longer have a nickname.");
        } else {
            if (!sender.hasPermission("icommands.nick.color")) {
                nick = stripColors(nick);
            }

            String error = validate(target, nick);
            if (error != null) {
                sender.sendMessage(error);
                return true;
            }

            plugin.setNick(target, nick);
            target.sendMessage("Your nickname is now " + stripColors(nick) + ".");
        }

        if (target != sender) {
            sender.sendMessage("Nickname changed.");
        }
        return true;
    }

    private String validate(Player target, String nick) {
        String plain = stripColors(nick);

        if (!ALLOWED_NICK.matcher(plain).matches()) {
            return "Nicknames must be alphanumeric.";
        }
        if (nick.length() > MAX_NICK_LENGTH) {
            return "That nickname is too long.";
        }
        if (isNickInUse(target, plain)) {
            return "That name is already in use.";
        }
        return null;
    }

    private boolean isNickInUse(Player target, String plain) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(target)) continue;

            String onlineNick = plugin.getNick(online);
            if (plain.equalsIgnoreCase(online.getName())
                    || (onlineNick != null && plain.equalsIgnoreCase(stripColors(onlineNick)))) {
                return true;
            }
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(plain);
        return offline != null && offline.hasPlayedBefore() && !offline.getUniqueId().equals(target.getUniqueId());
    }

    private static String stripColors(String nick) {
        return COLOR_CODES.matcher(nick).replaceAll("");
    }
}
