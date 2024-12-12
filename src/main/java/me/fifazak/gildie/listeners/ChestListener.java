package me.fifazak.gildie.listeners;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ChestListener implements Listener {
    private final GildiePlugin plugin;


    public ChestListener(GildiePlugin plugin) {
        this.plugin = plugin;
    }

    public interface GildiePlugin {
        Location getChestLocation();
        boolean isPlayerTeamLeader(Player player);
    }

    private static final Map<Material, Integer> REQUIRED_ITEMS = Map.of(
            Material.BAKED_POTATO, 64,
            Material.BREAD, 64,
            Material.DIAMOND, 64,
            Material.OBSIDIAN, 32,
            Material.SUGAR_CANE, 32,
            Material.TNT, 16,
            Material.ANVIL, 16,
            Material.ENCHANTING_TABLE, 4
    );

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();
        if (!isGuildChest(clickedLocation)) {
            return;
        }
    Player player = event.getPlayer();
    if (!plugin.isPlayerTeamLeader(player)) {
        player.sendMessage(ChatColor.RED + "Tylko lider drużyny może korzystać z tej skrzyni!");
        return;
    }
    Chest chest = (Chest) event.getClickedBlock().getState();
        Chest chest1 = (Chest) event.getClickedBlock().getState();
        Inventory inventory = chest.getInventory();

        if (hasRequiredItems(inventory)) {
            removeRequiredItems(inventory);
            player.sendMessage(ChatColor.GREEN + "Odblokowałeś możliwość stworzenia gildii! Użyj " + ChatColor.YELLOW + "/gildia" + ChatColor.GREEN + " aby wybrać teren.");
        } else {
            player.sendMessage(ChatColor.RED + "Nie masz wszystkich wymaganych przedmiotów w skrzynce!");
        }
    }

    private boolean isGuildChest(Location location) {
        return location != null && location.equals(plugin.getChestLocation());
    }

    private boolean hasRequiredItems(Inventory inventory) {
        return REQUIRED_ITEMS.entrySet().stream()
                .allMatch(entry -> inventory.containsAtLeast(new ItemStack(entry.getKey()), entry.getValue()));
    }

    private void removeRequiredItems(Inventory inventory) {
        REQUIRED_ITEMS.forEach((material, amount) ->
                inventory.removeItem(new ItemStack(material, amount))
        );
    }
}
