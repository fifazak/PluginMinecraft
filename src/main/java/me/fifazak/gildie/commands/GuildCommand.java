package me.fifazak.gildie.commands;

import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import me.fifazak.gildie.CustomFlags;
import me.fifazak.gildie.GildiePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand implements CommandExecutor {

    private final GildiePlugin plugin;

    public GuildCommand(GildiePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tylko gracze mogą używać tej komendy!");
            return true;
        }

        Player player = (Player) sender;
        switch (command.getName().toLowerCase()) {
            case "gildia":
                onGildiaCommand(player);
                break;
            case "gildiamake":
                onGildiaMakeCommand(player);
                break;
            case "gildiaund":
                onGildiaUndCommand(player);
                break;
            default:
                return false;
        }
        return true;
    }

    private void onGildiaCommand(Player player) {
        Team team = Team.getTeam(player);

        if (team == null || team.getTeamPlayer(player).getRank() != PlayerRank.OWNER) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może zaznaczyć teren gildii!");
            return;
        }

        // Zaznaczanie regionu
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Brak aktywnej sesji WorldEdit!");
            return;
        }

        BlockVector3 min = BlockVector3.at(player.getLocation().getBlockX() - 100, 0, player.getLocation().getBlockZ() - 100);
        BlockVector3 max = BlockVector3.at(player.getLocation().getBlockX() + 100, 255, player.getLocation().getBlockZ() + 100);

        try {
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 20, 1, 1, 1);
            player.sendMessage(ChatColor.GREEN + "Region został zaznaczony! Użyj /gildiamake, aby go utworzyć.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd przy zaznaczaniu regionu.");
        }
    }

    private void onGildiaMakeCommand(Player player) {
        Team team = Team.getTeam(player);

        if (team == null || team.getTeamPlayer(player).getRank() != PlayerRank.OWNER) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może utworzyć gildię!");
            return;
        }

        // Tworzenie regionu
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        String regionName = "guild_" + team.getName();

        if (manager == null) {
            player.sendMessage(ChatColor.RED + "Błąd wczytywania regionów!");
            return;
        }

        try {
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, BlockVector3.at(-100, 0, -100), BlockVector3.at(100, 255, 100));
            region.setFlag(CustomFlags.TEAM_PVP, StateFlag.State.DENY);
            region.setFlag(CustomFlags.TNT_PLACEMENT, StateFlag.State.DENY);
            region.setFlag(CustomFlags.FIRE_SPREAD, StateFlag.State.DENY);

            manager.addRegion(region);
            player.sendMessage(ChatColor.GREEN + "Region gildii został utworzony.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Nie udało się utworzyć regionu.");
        }
    }

    private void onGildiaUndCommand(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Anulowano zaznaczenie regionu gildii.");
    }
}
