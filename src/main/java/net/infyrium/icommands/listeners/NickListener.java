package net.infyrium.icommands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.infyrium.icommands.iCommandsMain;

public class NickListener implements Listener {

    private final iCommandsMain plugin;

    public NickListener(iCommandsMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.applyNick(event.getPlayer());
    }
}
