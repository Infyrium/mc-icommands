package net.infyrium.icommands.commands;

import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class ExtCommand extends TargetCommand {

    public ExtCommand(iCommandsMain plugin) {
        super(plugin, "ext");
    }

    @Override
    protected String execute(Player target) {
        target.setFireTicks(0);
        return "You have been extinguished!";
    }
}
