package me.fifazak.gildie;

import me.fifazak.gildie.commands.GuildCommand;
import me.fifazak.gildie.listeners.FireProtectionListener;
import me.fifazak.gildie.listeners.ChestListener;
import me.fifazak.gildie.listeners.ExplosionProtectionListener;
import me.fifazak.gildie.listeners.RegionListener;
import org.bukkit.plugin.java.JavaPlugin;

public class GildiePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Logowanie włączenia pluginu
        getLogger().info("Plugin Gildie został włączony!");

        // Rejestracja komend
        getCommand("gildia").setExecutor(new GuildCommand(this));
        getCommand("gildiamake").setExecutor(new GuildCommand(this));
        getCommand("gildiaund").setExecutor(new GuildCommand(this));

        // Rejestracja listenerów
        getServer().getPluginManager().registerEvents(new ExplosionProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChestListener(), this);
        getServer().getPluginManager().registerEvents(new FireProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new RegionListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Gildie został wyłączony!");
    }
}
