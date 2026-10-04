package net.infyrium.icommands.commands;

import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class FeedCommand extends TargetCommand {

    public FeedCommand(iCommandsMain plugin) {
        super(plugin, "feed");
    }

    @Override
    protected String execute(Player target) {
        target.setFoodLevel(20);
        target.setSaturation(10f);
        target.setExhaustion(0f);
        return "feed";
    }
}
