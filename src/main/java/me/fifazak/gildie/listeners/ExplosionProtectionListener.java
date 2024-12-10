package me.fifazak.gildie.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionProtectionListener implements Listener {

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
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() == org.bukkit.Material.TNT && isInGuildRegion(block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Nie możesz stawiać TNT na terenie gildii!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();

        if (isInGuildRegion(location)) {
            event.setCancelled(true);
            event.getEntity().getWorld().sendMessage(
                    ChatColor.RED + "Wybuchy na terenie gildii są zablokowane!"
            );
        }
    }
}
