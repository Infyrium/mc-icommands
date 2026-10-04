package net.infyrium.icommands;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import net.infyrium.icommands.commands.NickCommand;
import net.infyrium.icommands.commands.SpeedCommand;
import net.infyrium.icommands.commands.VanishCommand;
import net.infyrium.icommands.listeners.GodModeListener;
import net.infyrium.icommands.listeners.NickListener;
import net.infyrium.icommands.listeners.VanishListener;
import net.infyrium.icommands.managers.Messages;
import net.infyrium.icommands.utils.ConfigUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


public class iCommandsMain extends JavaPlugin {

    // Online vanished players, safe to read from the async server list ping
    private final Set<UUID> vanishedOnline = ConcurrentHashMap.newKeySet();

    private Messages messages;

    private NamespacedKey godModeKey;

    private NamespacedKey vanishKey;

    private NamespacedKey nickKey;

    @Override
    public void onEnable() {
        ConfigUpdater.backupIfOutdated(this, "config.yml");
        saveDefaultConfig();

        messages = new Messages(this);
        messages.load();

        godModeKey = new NamespacedKey(this, "god");
        vanishKey = new NamespacedKey(this, "vanish");
        nickKey = new NamespacedKey(this, "nick");

        Bukkit.getPluginManager().registerEvents(new GodModeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VanishListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NickListener(this), this);

        getCommand("feed").setExecutor(new FeedCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("speed").setExecutor(new SpeedCommand(this));
        getCommand("god").setExecutor(new GodCommand(this));
        getCommand("ext").setExecutor(new ExtCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("nick").setExecutor(new NickCommand(this));

        // Players are already online after a plugin reload
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isVanished(player)) {
                applyVanish(player);
            }
        }

        getLogger().info("Plugin has been enabled!");
        getLogger().info("Plugin developed by: " + String.join(", ", getPluginMeta().getAuthors()));
        getLogger().info("Website: " + getPluginMeta().getWebsite());
    }

    @Override
    public void onDisable() {
        // Vanish stays saved, but players can't stay hidden by a disabled plugin
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isVanished(player)) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.showPlayer(this, player);
                }
            }
        }

        getLogger().info("Plugin has been disabled!");
    }

    public Messages getMessages() {
        return messages;
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

    /**
     * Vanish is stored in the player's data, so it stays after rejoin.
     */
    public boolean isVanished(Player player) {
        return player.getPersistentDataContainer().has(vanishKey);
    }

    public void setVanished(Player player, boolean enabled) {
        if (enabled) {
            player.getPersistentDataContainer().set(vanishKey, PersistentDataType.BOOLEAN, true);
        } else {
            player.getPersistentDataContainer().remove(vanishKey);
        }
        applyVanish(player);
    }

    /**
     * Hides the player from everyone without icommands.vanish.see, or shows them again.
     */
    public void applyVanish(Player player) {
        boolean enabled = isVanished(player);

        if (enabled) {
            vanishedOnline.add(player.getUniqueId());
        } else {
            vanishedOnline.remove(player.getUniqueId());
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;

            if (enabled && !online.hasPermission("icommands.vanish.see")) {
                online.hidePlayer(this, player);
            } else {
                online.showPlayer(this, player);
            }
        }

        player.setSleepingIgnored(enabled);
        player.setCollidable(!enabled);
        player.setAffectsSpawning(!enabled);
    }

    public Set<UUID> getVanishedOnline() {
        return vanishedOnline;
    }

    /**
     * Returns the nickname with color codes, or null if the player has none.
     */
    public String getNick(Player player) {
        return player.getPersistentDataContainer().get(nickKey, PersistentDataType.STRING);
    }

    /**
     * Nickname is stored in the player's data, so it stays after rejoin. Null removes it.
     */
    public void setNick(Player player, String nick) {
        if (nick == null) {
            player.getPersistentDataContainer().remove(nickKey);
        } else {
            player.getPersistentDataContainer().set(nickKey, PersistentDataType.STRING, nick);
        }
        applyNick(player);
    }

    /**
     * Shows the nickname in chat and in the player list.
     */
    public void applyNick(Player player) {
        String nick = getNick(player);
        Component name = nick == null ? null : LegacyComponentSerializer.legacyAmpersand().deserialize(nick);
        player.displayName(name);
        player.playerListName(name);
    }
}
