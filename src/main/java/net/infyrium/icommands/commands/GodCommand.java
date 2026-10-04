package net.infyrium.icommands.commands;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import net.infyrium.icommands.iCommandsMain;

public class GodCommand extends TargetCommand {

    public GodCommand(iCommandsMain plugin) {
        super(plugin, "god");
    }

    @Override
    protected String execute(Player target) {
        boolean enabled = !plugin.isGodMode(target);
        plugin.setGodMode(target, enabled);

        if (enabled && !target.isDead()) {
            target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getValue());
            target.setFoodLevel(20);
        }

        return enabled ? "god-enabled" : "god-disabled";
    }
}
