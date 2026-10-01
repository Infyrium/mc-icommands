package net.infyrium.icommands.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

/**
 * Base for commands used as /command or /command [player].
 */
public abstract class TargetCommand implements CommandExecutor {

    protected final iCommandsMain plugin;

    private final String name;

    private final String permission;

    protected TargetCommand(iCommandsMain plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.permission = "icommands." + name;
    }

    /**
     * Applies the command to the target and returns the message for the target.
     */
    protected abstract String execute(Player target);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /" + label + " <player>");
                return true;
            }
            if (plugin.getConfig().getBoolean("settings.requirePermission." + name, true)
                    && !sender.hasPermission(permission)) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }
            target = player;
        } else {
            if (!sender.hasPermission(permission + ".others")) {
                sender.sendMessage("You don't have permission to use this command on other players.");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("Player '" + args[0] + "' is not online.");
                return true;
            }
        }

        String message = execute(target);
        target.sendMessage(message);
        if (target != sender) {
            sender.sendMessage(target.getName() + ": " + message);
        }
        return true;
    }
}
