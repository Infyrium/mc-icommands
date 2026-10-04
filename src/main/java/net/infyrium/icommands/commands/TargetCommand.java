package net.infyrium.icommands.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;
import net.infyrium.icommands.managers.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

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
     * Applies the command to the target and returns the message key.
     * The target gets the message with this key, a different sender gets the key with "-other".
     */
    protected abstract String execute(Player target);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = plugin.getMessages();
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "usage-player", Placeholder.unparsed("command", label));
                return true;
            }
            if (plugin.getConfig().getBoolean("settings.requirePermission." + name, true)
                    && !sender.hasPermission(permission)) {
                messages.send(sender, "no-permission");
                return true;
            }
            target = player;
        } else {
            if (!sender.hasPermission(permission + ".others")) {
                messages.send(sender, "no-permission-others");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            // Players hidden from the sender (e.g. vanished) look offline
            if (target == null || (sender instanceof Player player && !player.canSee(target))) {
                messages.send(sender, "player-not-online", Placeholder.unparsed("player", args[0]));
                return true;
            }
        }

        String key = execute(target);
        messages.send(target, key);
        if (target != sender) {
            messages.send(sender, key + "-other", Placeholder.unparsed("player", target.getName()));
        }
        return true;
    }
}
