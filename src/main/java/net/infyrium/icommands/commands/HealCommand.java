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
    protected Result execute(Player target) {
        if (target.isDead()) {
            return new Result("You can't be healed while dead.", "Cannot heal a dead player!");
        }

        target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setFireTicks(0);
        target.setRemainingAir(target.getMaximumAir());
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
        return new Result("You have been healed!", target.getName() + " has been healed!");
    }
}
