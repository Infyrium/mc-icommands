package net.infyrium.icommands.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import net.infyrium.icommands.iCommandsMain;

public class GodModeListener implements Listener {

    private final iCommandsMain plugin;

    public GodModeListener(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !plugin.isGodMode(player)) return;

        player.setFireTicks(0);
        player.setRemainingAir(player.getMaximumAir());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player player && plugin.isGodMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || !plugin.isGodMode(player)) return;

        player.setFoodLevel(20);
        player.setSaturation(10f);
        event.setCancelled(true);
    }

    // Players in god mode can't hurt other players without icommands.god.pvp
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker != null && plugin.isGodMode(attacker) && !attacker.hasPermission("icommands.god.pvp")) {
            event.setCancelled(true);
        }
    }
}
