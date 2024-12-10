package me.fifazak.gildie.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
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
            event.getEntity().getWorld().getPlayers().forEach(player -> player.sendMessage(ChatColor.RED + "Wybuchy są zablokowane na terenie gildii!"));
        }
    }

    private boolean isInGuildRegion(Location location) {
        com.sk89q.worldguard.WorldGuard wg = WorldGuard.getInstance(); // Zmieniono sposób importowania

        RegionContainer container = wg.getPlatform().getRegionContainer(); // Użyj getPlatform().getRegionContainer()
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
}