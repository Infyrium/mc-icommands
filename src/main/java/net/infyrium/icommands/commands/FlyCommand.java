package net.infyrium.icommands.commands;

import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class FlyCommand extends TargetCommand {

    public FlyCommand(iCommandsMain plugin) {
        super(plugin, "fly");
    }

    @Override
    protected String execute(Player target) {
        boolean enabled = !target.getAllowFlight();
        target.setAllowFlight(enabled);
        if (!enabled) {
            target.setFlying(false);
        }
        return "Flight " + (enabled ? "enabled" : "disabled") + "!";
    }
}
