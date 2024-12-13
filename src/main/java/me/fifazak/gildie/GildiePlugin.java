package me.fifazak.gildie;

import me.fifazak.gildie.commands.GuildCommand;
import me.fifazak.gildie.listeners.ChestListener;
import me.fifazak.gildie.listeners.RegionListener;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GildiePlugin extends JavaPlugin {

    private Location chestLocation;

    @Override
    public void onEnable() {
        getLogger().info("Plugin Gildie został włączony!");

        // Rejestracja flag
        CustomFlags.registerFlags();

        // Ładowanie konfiguracji
        saveDefaultConfig();
        loadChestLocation();

        // Rejestracja komend i listenerów
        getCommand("gildia").setExecutor(new GuildCommand(this));
        getCommand("gildiamake").setExecutor(new GuildCommand(this));
        getCommand("gildiaund").setExecutor(new GuildCommand(this));

        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new RegionListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Gildie został wyłączony!");
    }

    private void loadChestLocation() {
        FileConfiguration config = getConfig();
        chestLocation = new Location(
                getServer().getWorld(config.getString("specialChest.world", "world")),
                config.getDouble("specialChest.x", 0),
                config.getDouble("specialChest.y", 64),
                config.getDouble("specialChest.z", 0)
        );
    }

    public Location getChestLocation() {
        return chestLocation;
    }
}
