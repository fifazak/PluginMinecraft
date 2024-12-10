package me.fifazak.gildie.commands;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.team.TeamManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GuildCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GuildCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tylko gracze mogą używać tej komendy!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("gildia")) {
            onGildiaCommand(player);
        } else if (command.getName().equalsIgnoreCase("gildiamake")) {
            onGildiaMakeCommand(player);
        } else if (command.getName().equalsIgnoreCase("gildiaund")) {
            onGildiaUndCommand(player);
        }
        return true;
    }

    private void onGildiaCommand(Player player) {
        if (!isTeamOwner(player)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może tworzyć gildie!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Teren gildii został zaznaczony! Użyj /gildiamake aby utworzyć.");
    }

    private void onGildiaMakeCommand(Player player) {
        if (!isTeamOwner(player)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może tworzyć gildie!");
            return;
        }


        Team team = TeamManager.getInstance().getTeam(player);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Nie jesteś w żadnej drużynie!");
            return;
        }


        WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wg == null) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono pluginu WorldGuard!");
            return;
        }

        RegionContainer container = wg.getRegionContainer();
        RegionManager manager = container.get(wg.wrap(player.getWorld()));
        if (manager == null) {
            player.sendMessage(ChatColor.RED + "Błąd wczytywania regionów!");
            return;
        }

        String regionName = "guild_" + team.getName();
        if (manager.getRegion(regionName) != null) {
            player.sendMessage(ChatColor.RED + "Drużyna już ma przypisany region gildii!");
            return;
        }


        ProtectedRegion guildRegion = new ProtectedRegion(regionName);
        manager.addRegion(guildRegion);
        player.sendMessage(ChatColor.GREEN + "Gildia została stworzona i zabezpieczona!");
    }

    private void onGildiaUndCommand(Player player) {
        if (!isTeamOwner(player)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może anulować tworzenie gildii!");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Tworzenie gildii zostało anulowane.");
    }

    private boolean isTeamOwner(Player player) {
        Team team = TeamManager.getInstance().getTeam(player);
        return team != null && team.getOwner().equals(player.getUniqueId());
    }
}
