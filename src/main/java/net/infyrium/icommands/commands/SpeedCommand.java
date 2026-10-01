package net.infyrium.icommands.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class SpeedCommand implements TabExecutor {

    private static final List<String> TYPES = List.of("fly", "walk");

    private final iCommandsMain plugin;

    public SpeedCommand(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        if (plugin.getConfig().getBoolean("settings.requirePermission.speed", true)
                && !player.hasPermission("icommands.speed")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        // /speed <0-10> picks the type by whether the player is flying
        String type;
        String value;
        if (args.length == 1) {
            type = player.isFlying() ? "fly" : "walk";
            value = args[0];
        } else if (args.length == 2 && TYPES.contains(args[0].toLowerCase())) {
            type = args[0].toLowerCase();
            value = args[1];
        } else {
            player.sendMessage("Usage: /" + label + " [fly|walk] <0-10>");
            return true;
        }

        int speed;
        try {
            speed = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            speed = -1;
        }
        if (speed < 0 || speed > 10) {
            player.sendMessage("Speed must be a number from 0 to 10.");
            return true;
        }

        // Bukkit speed is 0.0-1.0 (default: walk 0.2, fly 0.1)
        if (type.equals("fly")) {
            player.setFlySpeed(speed / 10f);
            player.sendMessage("Fly speed set to " + speed + "!");
        } else {
            player.setWalkSpeed(speed / 10f);
            player.sendMessage("Walk speed set to " + speed + "!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return TYPES.stream().filter(type -> type.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
