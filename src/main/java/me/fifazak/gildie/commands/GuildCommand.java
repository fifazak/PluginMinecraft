package me.fifazak.gildie.commands;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.session.EditSession;
import com.sk89q.worldedit.session.LocalSession;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

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
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            LocalSession localSession = WorldEdit.getInstance().getSession(player);
            World world = editSession.getWorld();
            BlockVector3 playerLocation = BukkitAdapter.asBlockVector(player.getLocation());

            // Obliczanie rogów obszaru 200x200
            BlockVector3 min = playerLocation.subtract(100, 0, 100);
            BlockVector3 max = playerLocation.add(100, 0, 100);

            // Tworzenie zaznaczenia
            CuboidRegion selection = new CuboidRegion(world, min, max);
            localSession.setSelection(world, selection);

            // Wyświetlanie znaczników (przykład z cząsteczkami)
            player.getWorld().spawnParticle(Particle.FLAME, min.toLocation(player.getWorld()), 10, 0.5, 0.5, 0.5, 0);
            player.getWorld().spawnParticle(Particle.FLAME, max.toLocation(player.getWorld()), 10, 0.5, 0.5, 0.5, 0);

            player.sendMessage(ChatColor.GREEN + "Teren gildii został zaznaczony! Użyj /gildiamake aby utworzyć.");
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd podczas zaznaczania terenu.");
        }
    }

    private void onGildiaMakeCommand(Player player) {
        if (!isTeamOwner(player)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może tworzyć gildie!");
            return;
        }

        Team team = TeamManager.get().getTeam(player);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Nie jesteś w żadnej drużynie!");
            return;
        }

        // Pobierz instancję WorldGuard
        com.sk89q.worldguard.WorldGuard wg = WorldGuard.getInstance();

        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) {
            player.sendMessage(ChatColor.RED + "Błąd wczytywania regionów!");
            return;
        }

        String regionName = "guild_" + team.getName();
        if (manager.getRegion(regionName) != null) {
            player.sendMessage(ChatColor.RED + "Drużyna już ma przypisany region gildii!");
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            LocalSession localSession = WorldEdit.getInstance().getSession(player);
            Region selection = localSession.getSelection(editSession.getWorld());

            if (selection instanceof CuboidRegion) {
                CuboidRegion cuboidRegion = (CuboidRegion) selection;
                BlockVector3 min = cuboidRegion.getMinimumPoint();
                BlockVector3 max = cuboidRegion.getMaximumPoint();

                // Utwórz region
                ProtectedCuboidRegion guildRegion = new ProtectedCuboidRegion(regionName, min, max);

                // Ustaw flagi dla regionu
                guildRegion.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY); // Blokuj budowanie
                guildRegion.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);   // Blokuj PVP
                // Dodaj inne flagi według potrzeb

                // Dodaj członków drużyny do regionu (zezwól na budowanie)
                for (UUID member : team.getMembers().getUuids()) {
                    guildRegion.getMembers().addPlayer(member);
                }

                manager.addRegion(guildRegion);
                player.sendMessage(ChatColor.GREEN + "Gildia została stworzona i zabezpieczona!");

                // Usuń zaznaczenie po utworzeniu gildii
                localSession.setSelection(editSession.getWorld(), null);
            } else {
                player.sendMessage(ChatColor.RED + "Nieprawidłowe zaznaczenie!");
            }
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "Nie zaznaczyłeś terenu!");
        }
    }

    private void onGildiaUndCommand(Player player) {
        if (!isTeamOwner(player)) {
            player.sendMessage(ChatColor.RED + "Tylko lider drużyny może anulować tworzenie gildii!");
            return;
        }
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
            LocalSession localSession = WorldEdit.getInstance().getSession(player);
            com.sk89q.worldedit.world.World world = editSession.getWorld();

            // Usuwanie zaznaczenia
            localSession.setSelection(world, null);

            player.sendMessage(ChatColor.YELLOW + "Tworzenie gildii zostało anulowane.");
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "Wystąpił błąd podczas anulowania tworzenia gildii.");
        }
    }

    private boolean isTeamOwner(Player player) {
        Team team = TeamManager.get().getTeam(player);
        return team != null && team.getOwner().getUuid().equals(player.getUniqueId());
    }
}