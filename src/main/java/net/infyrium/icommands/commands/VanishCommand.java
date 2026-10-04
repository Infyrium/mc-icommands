package net.infyrium.icommands.commands;

import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class VanishCommand extends TargetCommand {

    public VanishCommand(iCommandsMain plugin) {
        super(plugin, "vanish");
    }

    @Override
    protected Result execute(Player target) {
        boolean enabled = !plugin.isVanished(target);
        plugin.setVanished(target, enabled);

        String state = enabled ? "enabled" : "disabled";
        String targetMessage = "Vanish " + state + "!";
        if (enabled) {
            targetMessage += " You are now invisible to other players.";
        }
        return new Result(targetMessage, "Vanish " + state + " for " + target.getName() + "!");
    }
}
