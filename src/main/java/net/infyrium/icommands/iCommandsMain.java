package net.infyrium.icommands;

import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.icommands.commands.ExtCommand;
import net.infyrium.icommands.commands.FeedCommand;
import net.infyrium.icommands.commands.FlyCommand;
import net.infyrium.icommands.commands.GodCommand;
import net.infyrium.icommands.commands.HealCommand;
import net.infyrium.icommands.commands.SpeedCommand;


public class iCommandsMain extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
}
