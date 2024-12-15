package me.fifazak.gildie;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class CustomFlags {

    public static StateFlag TEAM_PVP;
    public static StateFlag TNT_PLACEMENT;
    public static StateFlag FIRE_SPREAD;

    public static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            TEAM_PVP = new StateFlag("team-pvp", false);
            TNT_PLACEMENT = new StateFlag("tnt-placement", false);
            FIRE_SPREAD = new StateFlag("fire-spread", false);

            registry.register(TEAM_PVP);
            registry.register(TNT_PLACEMENT);
            registry.register(FIRE_SPREAD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
