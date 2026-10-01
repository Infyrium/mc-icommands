package net.infyrium.icommands.commands;

import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class GodCommand extends TargetCommand {

    public GodCommand(iCommandsMain plugin) {
        super(plugin, "god");
    }

    @Override
    protected Result execute(Player target) {
        boolean enabled = !target.isInvulnerable();
        target.setInvulnerable(enabled);
        String state = enabled ? "enabled" : "disabled";
        return new Result("God mode " + state + "!", "God mode " + state + " for " + target.getName() + "!");
    }
}
