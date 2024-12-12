package me.fifazak.gildie;

import me.fifazak.gildie.commands.GuildCommand;
import me.fifazak.gildie.listeners.FireProtectionListener;
import me.fifazak.gildie.listeners.ChestListener;
import me.fifazak.gildie.listeners.ExplosionProtectionListener;
import me.fifazak.gildie.listeners.RegionListener;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class GildiePlugin extends JavaPlugin {

    private Location chestLocation = null;

    @Override
    public void onEnable() {
        // Logowanie włączenia pluginu
        getLogger().info("Plugin Gildie został włączony!");

        // Rejestracja komend
        if (getCommand("gildia") != null) {
            getCommand("gildia").setExecutor(new GuildCommand(this));
        }

        if (getCommand("gildiamake") != null) {
            getCommand("gildiamake").setExecutor(new GuildCommand(this));
        }

        if (getCommand("gildiaund") != null) {
            getCommand("gildiaund").setExecutor(new GuildCommand(this));
        }

        // Rejestracja listenerów
        getServer().getPluginManager().registerEvents(new ExplosionProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChestListener(), this);
        getServer().getPluginManager().registerEvents(new FireProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new RegionListener(), this);

        // Wczytaj lokalizację skrzynki z config.yml
        chestLocation = new Location(
                getServer().getWorld(getConfig().getString("specialChest.world", "world")),

                getConfig().getInt("specialChest.x", 0),

                getConfig().getInt("specialChest.y", 64),

                getConfig().getInt("specialChest.z", 0)
        );
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Gildie został wyłączony!");
    }

    public Location getChestLocation() {
        return chestLocation;
    }
}