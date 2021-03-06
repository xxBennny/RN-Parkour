package com.renatusnetwork.parkour.gameplay;
import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.PlayerHider;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SpectatorHandler {

    public static void startScheduler(Plugin plugin) {

        // update any current spectators every second
        new BukkitRunnable() {
            public void run() {
                updateSpectators();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public static void spectateToPlayer(Player spectator, Player player, boolean initialSpectate) {
        if (player.isOnline() && spectator.isOnline()) {

            spectator.teleport(player.getLocation());

            // this is done AFTER teleport to override some world changes that can happen
            if (initialSpectate) {
                spectator.setAllowFlight(true);
                spectator.setFlying(true);
            }

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    "", Utils.translate("&7Teleported to " + player.getDisplayName() +
                            "&7, use &2/spectate &7 to stop"));
        }
    }

    public static void respawnToLastLocation(PlayerStats playerStats) {
        Location loc = playerStats.getSpectateSpawn();
        Player player = playerStats.getPlayer();

        if (loc != null) {

            player.teleport(loc);
            TitleAPI.sendTitle(
                    player, 10, 40, 10,
                    "",
                    Utils.translate("&7You are no longer spectating anyone"));
            playerStats.resetSpectateSpawn();

            // region null check
            ProtectedRegion region = WorldGuard.getRegion(loc);
            if (region != null) {

                Level level = Parkour.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level
                if (level != null) {
                    playerStats.setLevel(level);

                    // if elytra level, toggle on
                    if (playerStats.getLevel().isElytraLevel())
                        Parkour.getStatsManager().toggleOnElytra(playerStats);
                }
            }
        }
    }

    public static void setSpectatorMode(PlayerStats spectatorStats, PlayerStats playerStats, boolean initialSpectate) {

        Player spectator = spectatorStats.getPlayer();
        Player player = playerStats.getPlayer();

        spectatorStats.setPlayerToSpectate(playerStats);

        // in case they /spectate while spectating
        if (initialSpectate) {
            spectatorStats.setSpectateSpawn(spectator.getLocation());
            Parkour.getStatsManager().toggleOffElytra(spectatorStats);
            PlayerHider.hidePlayer(spectator, true);
        }

        spectateToPlayer(spectator, player, initialSpectate);
    }

    public static void removeSpectatorMode(PlayerStats spectatorStats) {

        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);
        player.setFlying(false);
        player.setAllowFlight(false);
        PlayerHider.showPlayer(player, true);
        respawnToLastLocation(spectatorStats);
    }

    public static void updateSpectators() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                updateSpectator(playerStats);
    }

    public static void updateSpectator(PlayerStats spectator) {
        PlayerStats playerStats = spectator.getPlayerToSpectate();

        if (playerStats != null && playerStats.getPlayer().isOnline() && playerStats.isSpectatable() &&
            !playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            if (!playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(spectator.getPlayer().getWorld().getName()) ||
                spectator.getPlayer().getLocation().distance(playerStats.getPlayer().getLocation()) > 20)
                spectateToPlayer(spectator.getPlayer(), playerStats.getPlayer(), false);
        } else {
            removeSpectatorMode(spectator);
        }
    }

    public static void shutdown() {
        for (Map.Entry<String, PlayerStats> entry : Parkour.getStatsManager().getPlayerStats().entrySet()) {
            
            PlayerStats playerStats = entry.getValue();
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                removeSpectatorMode(playerStats);
        }
    }
}
