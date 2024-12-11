package me.fifazak.gildie.commands;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.booksaw.betterTeams.team.TeamManager;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
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

import java.util.UUID;

public class GuildCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GuildCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final String ONLY_TEAM_OWNER = ChatColor.RED + "Tylko lider drużyny może %s!";
    private static final String REGION_CREATED = ChatColor.GREEN + "Gildia została stworzona i zabezpieczona!";
    private static final String REGION_ALREADY_EXISTS = ChatColor.RED + "Drużyna już ma przypisany region gildii!";
    private static final String SELECTION_ERROR = ChatColor.RED + "Nie zaznaczyłeś poprawnego terenu!";
    private static final String NO_TEAM = ChatColor.RED + "Nie jesteś w żadnej drużynie!";
    private static final String WORLDGUARD_ERROR = ChatColor.RED + "Błąd wczytywania regionów WorldGuard!";

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
        TeamManager teamManager = TeamManager.getInstance();
        Team team = teamManager.getTeam(player);

        if (team == null || !team.getOwner().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(String.format(ONLY_TEAM_OWNER, "tworzyć gildie"));
            return;
        }

        try {
            LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            if (localSession == null) {
                player.sendMessage(ChatColor.RED + "Nie masz aktywnej sesji WorldEdit!");
                return;
            }

            World adaptedWorld = BukkitAdapter.adapt(player.getWorld());
            BlockVector3 playerLocation = BukkitAdapter.asBlockVector(player.getLocation());

            // Obliczanie rogów obszaru 200x200
            BlockVector3 min = playerLocation.subtract(100, 0, 100);
            BlockVector3 max = playerLocation.add(100, 0, 100);

            // Ustawienie regionu w selektorze
            localSession.getRegionSelector(adaptedWorld).select(min, max);

            // Wyświetlanie cząsteczek na pozycji gracza
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.5, 0.5, 0.5);

            player.sendMessage(ChatColor.GREEN + "Teren gildii został zaznaczony! Użyj /gildiamake aby utworzyć.");
        } catch (Exception e) {
            player.sendMessage(SELECTION_ERROR);
        }
    }

    private void onGildiaMakeCommand(Player player) {
        TeamManager teamManager = TeamManager.getInstance();
        Team team = teamManager.getTeam(player);

        if (team == null) {
            player.sendMessage(NO_TEAM);
            return;
        }

        if (!team.getOwner().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(String.format(ONLY_TEAM_OWNER, "tworzyć gildie"));
            return;
        }

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) {
            player.sendMessage(WORLDGUARD_ERROR);
            return;
        }

        String regionName = "guild_" + team.getName();
        if (manager.getRegion(regionName) != null) {
            player.sendMessage(REGION_ALREADY_EXISTS);
            return;
        }

        try {
            LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region selection = localSession.getRegionSelector(BukkitAdapter.adapt(player.getWorld())).getRegion();

            if (selection instanceof CuboidRegion) {
                CuboidRegion cuboidRegion = (CuboidRegion) selection;
                ProtectedCuboidRegion guildRegion = new ProtectedCuboidRegion(regionName,
                        cuboidRegion.getMinimumPoint(), cuboidRegion.getMaximumPoint());

                // Ustaw flagi dla regionu
                guildRegion.setFlag(Flags.BUILD, StateFlag.State.DENY);
                guildRegion.setFlag(Flags.PVP, StateFlag.State.DENY);

                // Dodaj członków drużyny
                for (TeamPlayer member : team.getMembers()) {
                    guildRegion.getMembers().addPlayer(member.getUniqueId());
                }

                manager.addRegion(guildRegion);
                player.sendMessage(REGION_CREATED);
            } else {
                player.sendMessage(SELECTION_ERROR);
            }
        } catch (Exception e) {
            player.sendMessage(SELECTION_ERROR);
        }
    }

    private void onGildiaUndCommand(Player player) {
        TeamManager teamManager = TeamManager.getInstance();
        Team team = teamManager.getTeam(player);

        if (team == null || !team.getOwner().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(String.format(ONLY_TEAM_OWNER, "anulować tworzenie gildii"));
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
        }
    }
}
