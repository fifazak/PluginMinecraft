package me.fifazak.gildie.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class FireProtectionListener implements Listener {

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Location location = event.getBlock().getLocation();
        if (isInGuildRegion(location)) {
            event.setCancelled(true);
            event.getBlock().getWorld().getPlayers().forEach(player ->
                    player.sendMessage(ChatColor.RED + "Rozprzestrzenianie się ognia zostało zablokowane na terenie gildii!")
            );
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();
        if (isInGuildRegion(location)) {
            event.setCancelled(true);
            event.getEntity().getWorld().sendMessage(ChatColor.RED + "Wybuchy są zablokowane na terenie gildii!");
        }
    }

    private boolean isInGuildRegion(Location location) {
        WorldGuardPlugin wg = (WorldGuardPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null) return false;

        RegionContainer container = wg.getRegionContainer();
        RegionManager manager = container.get(wg.wrap(location.getWorld()));
        if (manager == null) return false;

        ApplicableRegionSet regions = manager.getApplicableRegions(location.toVector());
        for (ProtectedRegion region : regions) {
            if (region.getId().startsWith("guild_")) {
                return true;
            }
        }
        return false;
    }
}
