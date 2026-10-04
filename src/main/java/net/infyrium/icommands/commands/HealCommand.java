package net.infyrium.icommands.commands;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import net.infyrium.icommands.iCommandsMain;

public class HealCommand extends TargetCommand {

    public HealCommand(iCommandsMain plugin) {
        super(plugin, "heal");
    }

    @Override
    protected String execute(Player target) {
        if (target.isDead()) {
            return "heal-dead";
        }

        target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setFireTicks(0);
        target.setRemainingAir(target.getMaximumAir());
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
        return "heal";
    }
}
