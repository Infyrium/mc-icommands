package net.infyrium.icommands.managers;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.icommands.utils.Colors;
import net.infyrium.icommands.utils.ConfigUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Messages from messages.yml in MiniMessage format. Legacy color codes work too.
 */
public class Messages {

    private static final String FILE_NAME = "messages.yml";

    private final JavaPlugin plugin;

    private FileConfiguration config;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ConfigUpdater.backupIfOutdated(plugin, FILE_NAME);

        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        // Keys missing in the server file fall back to the defaults from the jar
        InputStream defaults = plugin.getResource(FILE_NAME);
        if (defaults != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    public Component get(String key, TagResolver... placeholders) {
        return MiniMessage.miniMessage().deserialize(getRaw(key), placeholders);
    }

    /**
     * Sends the message. An empty message in messages.yml is not sent at all.
     */
    public void send(CommandSender sender, String key, TagResolver... placeholders) {
        String message = getRaw(key);
        if (message.isEmpty()) return;

        sender.sendMessage(MiniMessage.miniMessage().deserialize(message, placeholders));
    }

    private String getRaw(String key) {
        return Colors.toMiniMessage(config.getString("messages." + key, key));
    }
}
