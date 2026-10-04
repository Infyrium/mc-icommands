package net.infyrium.icommands.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.EnderChest;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;

import net.infyrium.icommands.iCommandsMain;

public class VanishListener implements Listener {

    private final iCommandsMain plugin;

    // Players who opened a container silently and have not closed it yet
    private final Set<UUID> silentContainers = new HashSet<>();

    // Players switched to spectator for one tick while a silent container closes
    private final Map<UUID, Runnable> pendingRestores = new HashMap<>();

    public VanishListener(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    // HIGHEST so the join message is removed after other plugins set it
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.isVanished(player)) {
            event.joinMessage(null);
            plugin.applyVanish(player);
            plugin.getMessages().send(player, "vanish-still");
        }

        if (player.hasPermission("icommands.vanish.see")) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && plugin.isVanished(online)) {
                player.hidePlayer(plugin, online);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        silentContainers.remove(player.getUniqueId());

        // Don't let the player be saved in spectator mode
        Runnable restore = pendingRestores.remove(player.getUniqueId());
        if (restore != null) {
            restore.run();
        }

        if (plugin.isVanished(player)) {
            event.quitMessage(null);
            plugin.getVanishedOnline().remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.isVanished(event.getPlayer())) {
            event.deathMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        if (plugin.isVanished(event.getPlayer())) {
            event.message(null);
        }
    }

    // Vanished players are not counted or listed in the server list
    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        Set<UUID> vanished = plugin.getVanishedOnline();
        if (vanished.isEmpty()) return;

        event.setNumPlayers(Math.max(0, event.getNumPlayers() - vanished.size()));
        event.getListedPlayers().removeIf(listed -> vanished.contains(listed.id()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && cantPickup(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupExperience(PlayerPickupExperienceEvent event) {
        if (cantPickup(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        if (cantPickup(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean cantPickup(Player player) {
        return plugin.isVanished(player) && !player.hasPermission("icommands.vanish.pickup");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && plugin.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockReceiveGame(BlockReceiveGameEvent event) {
        if (event.getEntity() instanceof Player player && plugin.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    // Arrows, snowballs and other projectiles fly through vanished players
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player player && plugin.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    // Vanished players can't hurt other players without icommands.vanish.pvp
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker != null && plugin.isVanished(attacker) && !attacker.hasPermission("icommands.vanish.pvp")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isVanished(player)) return;

        // Pressure plates, tripwires, farmland
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (player.isSneaking() && event.getItem() != null) return;

        Block block = event.getClickedBlock();
        BlockState state = block == null ? null : block.getState();
        if (!(state instanceof Lidded)) return;

        // Chests, barrels and shulker boxes open without the lid animation and sound
        if (state instanceof EnderChest) {
            event.setCancelled(true);
            player.openInventory(player.getEnderChest());
        } else if (state instanceof Container container) {
            event.setCancelled(true);
            runAsSpectator(player, () -> player.openInventory(container.getInventory()));
            silentContainers.add(player.getUniqueId());
        }
    }

    // The container must also be closed as a spectator, or its open counter breaks
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!silentContainers.remove(player.getUniqueId())) return;
        if (event.getReason() == InventoryCloseEvent.Reason.DISCONNECT) return;

        GameMode gameMode = player.getGameMode();
        boolean allowFlight = player.getAllowFlight();
        boolean flying = player.isFlying();

        player.setGameMode(GameMode.SPECTATOR);
        pendingRestores.put(player.getUniqueId(), () -> restore(player, gameMode, allowFlight, flying));
        Bukkit.getScheduler().runTask(plugin, () -> {
            Runnable restore = pendingRestores.remove(player.getUniqueId());
            if (restore != null) {
                restore.run();
            }
        });
    }

    /**
     * The game does not animate containers opened by spectators,
     * so the player is switched to spectator just for the action.
     */
    private void runAsSpectator(Player player, Runnable action) {
        GameMode gameMode = player.getGameMode();
        boolean allowFlight = player.getAllowFlight();
        boolean flying = player.isFlying();

        player.setGameMode(GameMode.SPECTATOR);
        action.run();
        restore(player, gameMode, allowFlight, flying);
    }

    private void restore(Player player, GameMode gameMode, boolean allowFlight, boolean flying) {
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight && flying);
    }
}
