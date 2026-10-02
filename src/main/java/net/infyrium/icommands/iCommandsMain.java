package net.infyrium.icommands;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.icommands.commands.ExtCommand;
import net.infyrium.icommands.commands.FeedCommand;
import net.infyrium.icommands.commands.FlyCommand;
import net.infyrium.icommands.commands.GodCommand;
import net.infyrium.icommands.commands.HealCommand;
import net.infyrium.icommands.commands.SpeedCommand;


public class iCommandsMain extends JavaPlugin {

    private NamespacedKey godModeKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        godModeKey = new NamespacedKey(this, "god");

        Bukkit.getPluginManager().registerEvents(new GodModeListener(this), this);

        getCommand("feed").setExecutor(new FeedCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand(this));
        getCommand("god").setExecutor(new GodCommand(this));
        getCommand("ext").setExecutor(new ExtCommand(this));

        getLogger().info("Plugin has been enabled!");
        getLogger().info("Plugin developed by: " + String.join(", ", getPluginMeta().getAuthors()));
        getLogger().info("Website: " + getPluginMeta().getWebsite());
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled!");
    }

    /**
     * God mode is stored in the player's data, so it stays after rejoin.
     */
    public boolean isGodMode(Player player) {
        return player.getPersistentDataContainer().has(godModeKey);
    }

    public void setGodMode(Player player, boolean enabled) {
        if (enabled) {
            player.getPersistentDataContainer().set(godModeKey, PersistentDataType.BOOLEAN, true);
        } else {
            player.getPersistentDataContainer().remove(godModeKey);
            // Older versions used the vanilla invulnerable flag
            player.setInvulnerable(false);
        }
    }
}
