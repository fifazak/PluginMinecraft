package me.fifazak.gildie.commands;

import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class GuildCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GuildCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final String ONLY_TEAM_OWNER = ChatColor.RED + "Tylko lider drużyny może %s!";
    private static final String REGION_CREATED = ChatColor.GREEN + "Gildia została stworzona i zabezpieczona!";
    private static final String REGION_ALREADY_EXISTS = ChatColor.RED + "Drużyna już ma przypisany region gildii!";
    private static final String SELECTION_ERROR = ChatColor.RED + "Nie zaznaczyłeś poprawnego terenu!";

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

    private void onGildiaUndCommand(Player player) {

        Team team = Team.getTeam(player.getUniqueId());

        if (team == null || !team.getRank(player.getUniqueId()).equals(PlayerRank.OWNER)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może anulować tworzenie gildii!");
            return;
        }

        try {
            LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            if (localSession == null) {
                player.sendMessage(ChatColor.RED + "Nie masz aktywnej sesji WorldEdit!");
                return;
            }
            localSession.getRegionSelector(localSession.getSelectionWorld()).clear();

            player.sendMessage(ChatColor.YELLOW + "Tworzenie gildii zostało anulowane.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd podczas anulowania tworzenia gildii.");
            e.printStackTrace();
        }
    }

    private void onGildiaCommand(Player player) {
        Team team = Team.getTeam(player.getUniqueId());

        if (team == null || !team.getRank(player.getUniqueId()).equals(PlayerRank.OWNER)) {
            player.sendMessage(String.format("Tylko lider drużyny może %s.", "tworzyć gildie"));
            return;
        }

        try {
            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(player.getWorld());
            BlockVector3 playerLocation = BukkitAdapter.asBlockVector(player.getLocation());

            BlockVector3 min = playerLocation.subtract(200, 1000, 200);
            BlockVector3 max = playerLocation.add(200, 1000, 200);

            LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            if (localSession == null) {
                player.sendMessage(ChatColor.RED + "Nie masz aktywnej sesji WorldEdit!");
                return;
            }

            // Poprawiona linia - używamy select zamiast selectPrimary
            localSession.getRegionSelector(adaptedWorld).select(min, max);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.5, 0.5, 0.5);

            player.sendMessage(ChatColor.GREEN + "Teren gildii został zaznaczony! Użyj /gildiamake aby utworzyć.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd podczas zaznaczania terenu.");
            e.printStackTrace();
        }
    }

    private void onGildiaMakeCommand(Player player) {
        Team team = Team.getTeam(player.getUniqueId());

        if (team == null) {
            player.sendMessage(ChatColor.RED + "Nie jesteś w żadnej drużynie!");
            return;
        }

        if (!team.getRank(player.getUniqueId()).equals(PlayerRank.OWNER)) {
            player.sendMessage(String.format("Tylko lider drużyny może %s.", "tworzyć gildie"));
            return;
        }

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) {
            player.sendMessage(ChatColor.RED + "Błąd wczytywania regionów!");
            return;
        }

        String regionName = "guild_" + team.getName();
        if (manager.getRegion(regionName) != null) {
            player.sendMessage(ChatColor.RED + "Region o tej nazwie już istnieje!");
            return;
        }

        try {
            LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            CuboidRegion selection = (CuboidRegion) localSession.getRegionSelector(BukkitAdapter.adapt(player.getWorld())).getRegion();

            ProtectedCuboidRegion guildRegion = new ProtectedCuboidRegion(regionName,
                    selection.getMinimumPoint(), selection.getMaximumPoint());

            guildRegion.setFlag(Flags.BUILD, StateFlag.State.DENY);
            guildRegion.setFlag(Flags.PVP, StateFlag.State.DENY);

            manager.addRegion(guildRegion);
            player.sendMessage(ChatColor.GREEN + "Region gildii został utworzony.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd podczas tworzenia regionu.");
            e.printStackTrace();
        }
    }
}
