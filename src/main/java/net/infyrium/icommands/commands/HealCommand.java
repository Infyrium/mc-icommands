package net.infyrium.icommands.commands;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class HealCommand extends TargetCommand {

    public HealCommand(iCommandsMain plugin) {
        super(plugin, "heal");
    }

    @Override
    protected String execute(Player target) {
        target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setFireTicks(0);
        return "You have been healed!";
    }
}
