package me.fifazak.gildie.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class RegionListener implements Listener {

    private boolean isInGuildRegion(Location location) {
        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) return false;

        RegionContainer container = wg.getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        for (ProtectedRegion region : regions) {
            if (region.getId().startsWith("guild_")) {
                return true;
            }
        }
        return false;
    }

    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        if (isInGuildRegion(to)) {
            player.sendMessage(ChatColor.GREEN + "Wszedłeś na teren gildii!");
        }
    }
}
