package net.infyrium.icommands.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class SpeedCommand implements TabExecutor {

    private static final List<String> TYPES = List.of("walk", "fly", "1", "1.5", "1.75", "2");

    private static final List<String> SPEEDS = List.of("1", "1.5", "1.75", "2");

    private static final float DEFAULT_WALK_SPEED = 0.2f;

    private static final float DEFAULT_FLY_SPEED = 0.1f;

    private static final float MAX_SPEED = 0.8f;

    private final iCommandsMain plugin;

    public SpeedCommand(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String usage = "Usage: /" + label + " [fly|walk] <0-10> [player]";

        // /speed <speed> picks the type by whether the player is flying
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(usage);
                return true;
            }
            if (!hasPermission(player)) return true;

            Float speed = parseSpeed(args[0]);
            if (speed == null) {
                player.sendMessage(usage);
                return true;
            }
            setSpeed(player, player.isFlying(), speed);
            player.sendMessage(message(player.isFlying(), speed) + "!");
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(usage);
            return true;
        }

        Boolean fly = parseType(args[0]);
        Float speed = parseSpeed(args[1]);
        if (fly == null || speed == null) {
            sender.sendMessage(usage);
            return true;
        }

        if (args.length == 3) {
            if (!sender.hasPermission("icommands.speed.others")) {
                sender.sendMessage("You don't have permission to use this command on other players.");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null || (sender instanceof Player player && !player.canSee(target))) {
                sender.sendMessage("Player '" + args[2] + "' is not online.");
                return true;
            }
            setSpeed(target, fly, speed);
            target.sendMessage(message(fly, speed) + "!");
            if (target != sender) {
                sender.sendMessage(message(fly, speed) + " for " + target.getName() + "!");
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(usage);
            return true;
        }
        if (!hasPermission(player)) return true;

        setSpeed(player, fly, speed);
        player.sendMessage(message(fly, speed) + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(TYPES, args[0]);
        }
        if (args.length == 2) {
            return filter(SPEEDS, args[1]);
        }
        if (args.length == 3 && sender.hasPermission("icommands.speed.others")) {
            return filter(Bukkit.getOnlinePlayers().stream()
                    .filter(online -> !(sender instanceof Player player) || player.canSee(online))
                    .map(Player::getName).toList(), args[2]);
        }
        return List.of();
    }

    private boolean hasPermission(Player player) {
        if (plugin.getConfig().getBoolean("settings.requirePermission.speed", true)
                && !player.hasPermission("icommands.speed")) {
            player.sendMessage("You don't have permission to use this command.");
            return false;
        }
        return true;
    }

    private static Boolean parseType(String type) {
        type = type.toLowerCase();
        if (type.contains("fly") || type.equals("f")) return true;
        if (type.contains("walk") || type.contains("run") || type.equals("w") || type.equals("r")) return false;
        return null;
    }

    /**
     * Parses speed 0-10, where 1 is the default speed.
     */
    private static Float parseSpeed(String value) {
        try {
            float speed = Float.parseFloat(value);
            return Math.max(0.0001f, Math.min(speed, 10f));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts speed 0-10 to Bukkit speed: below 1 is slower than default,
     * 1 is default, 10 is the max speed.
     */
    private static void setSpeed(Player player, boolean fly, float speed) {
        float defaultSpeed = fly ? DEFAULT_FLY_SPEED : DEFAULT_WALK_SPEED;
        float realSpeed = speed < 1f
                ? defaultSpeed * speed
                : defaultSpeed + (speed - 1f) / 9f * (MAX_SPEED - defaultSpeed);

        if (fly) {
            player.setFlySpeed(realSpeed);
        } else {
            player.setWalkSpeed(realSpeed);
        }
    }

    private static String message(boolean fly, float speed) {
        String value = speed == (int) speed ? String.valueOf((int) speed) : String.valueOf(speed);
        return (fly ? "Fly" : "Walk") + " speed set to " + value;
    }

    private static List<String> filter(List<String> options, String input) {
        return options.stream().filter(option -> option.toLowerCase().startsWith(input.toLowerCase())).toList();
    }
}
