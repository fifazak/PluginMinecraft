package me.fifazak.gildie.listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestListener implements Listener {

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) event.getClickedBlock().getState();
            Inventory inventory = chest.getInventory();

            if (hasRequiredItems(inventory)) {
                removeRequiredItems(inventory);
                player.sendMessage("Odblokowałeś możliwość stworzenia gildii! Użyj /gildia aby wybrać teren.");
            } else {
                player.sendMessage("Nie masz wszystkich wymaganych przedmiotów w skrzynce!");
            }
        }
    }

    private boolean hasRequiredItems(Inventory inventory) {
        return inventory.containsAtLeast(new ItemStack(Material.BAKED_POTATO), 64) &&
                inventory.containsAtLeast(new ItemStack(Material.BREAD), 64) &&
                inventory.containsAtLeast(new ItemStack(Material.DIAMOND), 64) &&
                inventory.containsAtLeast(new ItemStack(Material.OBSIDIAN), 32) &&
                inventory.containsAtLeast(new ItemStack(Material.SUGAR_CANE), 32) &&
                inventory.containsAtLeast(new ItemStack(Material.TNT), 16) &&
                inventory.containsAtLeast(new ItemStack(Material.ANVIL), 16) &&
                inventory.containsAtLeast(new ItemStack(Material.ENCHANTING_TABLE), 4);
    }

    private void removeRequiredItems(Inventory inventory) {
        inventory.removeItem(new ItemStack(Material.BAKED_POTATO, 64));
        inventory.removeItem(new ItemStack(Material.BREAD, 64));
        inventory.removeItem(new ItemStack(Material.DIAMOND, 64));
        inventory.removeItem(new ItemStack(Material.OBSIDIAN, 32));
        inventory.removeItem(new ItemStack(Material.SUGAR_CANE, 32));
        inventory.removeItem(new ItemStack(Material.TNT, 16));
        inventory.removeItem(new ItemStack(Material.ANVIL, 16));
        inventory.removeItem(new ItemStack(Material.ENCHANTING_TABLE, 4));
    }
}
