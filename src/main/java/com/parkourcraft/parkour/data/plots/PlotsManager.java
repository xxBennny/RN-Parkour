package com.parkourcraft.parkour.data.plots;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlotsManager {

    private List<Plot> plotList = new ArrayList<>();

    public PlotsManager() {
    }

    // player param version
    public void add(Player player) {
        Plot rank = new Plot(player, player.getLocation());
        plotList.add(rank);
    }

    // string ver
    public void add(String playerName, String playerUUID, Location spawnLoc) {
        Plot rank = new Plot(playerName, playerUUID, spawnLoc);
        plotList.add(rank);
    }

    public Plot get(String playerName) {
        for (Plot plot : plotList)
            if (plot.getOwnerName().equalsIgnoreCase(playerName))
                return plot;

        return null;
    }

    public Plot getFromUUID(String playerUUID) {
        for (Plot plot : plotList)
            if (plot.getOwnerUUID().equalsIgnoreCase(playerUUID))
                return plot;

        return null;
    }

    public boolean existsFromUUID(String playerUUID) {
        return (getFromUUID(playerUUID) != null);
    }

    public boolean exists(String playerName) {
        return (get(playerName) != null);
    }

    public List<Plot> getPlots() {
        return plotList;
    }

    public void remove(String playerUUID) {
        if (existsFromUUID(playerUUID))
            plotList.remove(playerUUID);
    }

    // creation algorithm
    public void createPlot(Player player) {

        // run algorithm in async
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(Parkour.getPlugin(), () -> {

        String result = findNextFreePlot(player.getUniqueId().toString());

        // run teleport and creation in sync
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Parkour.getPlugin(), () -> {
            if (result != null && !result.equalsIgnoreCase("")) {

                // if they have a plot
                if (result.equalsIgnoreCase("hasPlot")) {
                    player.sendMessage(Utils.translate("&cYou already have a plot!"));
                    return;
                }

                String[] split = result.split(":");
                // teleport to found plot!

                Location loc = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                        Double.parseDouble(split[0]), Parkour.getSettingsManager().player_submitted_plot_default_y,
                        Double.parseDouble(split[1]), player.getLocation().getYaw(), player.getLocation().getPitch());

                // set bedrock -1 where they teleport
                loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);

                player.teleport(loc.clone().add(0.5, 0, 0.5));
                // add data
                add(player);
                Plots_DB.addPlot(player, loc);
                player.sendMessage(Utils.translate("&7Your &a&lPlot &7has been created!" +
                                                        " &7Type &a/plot home &7to get back!"));
                }
            });
        });
    }

    public void deletePlot(Player player) {

        Plot plot = getFromUUID(player.getUniqueId().toString());

        if (plot != null) {
            // remove from cache and teleport to spawn
            plotList.remove(plot);
            Plots_DB.removePlot(player);
            // if they are in plot world, teleport them
            if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
                player.teleport(Parkour.getSettingsManager().spawn_location);

            player.sendMessage(Utils.translate("&cYou have deleted your plot"));
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot!"));
        }
    }

    /*
      This method works in complete async and operates in an infinite (biggest int) loop.
      It will start at 0 + half plot width, 0 + half plot width then keeps track of which way it
      is going so it can check if there is a plot to the right of it based on direction. If there is no
      plot to the right, it turns right otherwise if there is a plot to the right, it moves forward.
     */
    private String findNextFreePlot(String playerUUID) {

        int max = Integer.MAX_VALUE;
        int plotWidth = Parkour.getSettingsManager().player_submitted_plot_width;

        List<String> plots = Plots_DB.getPlotCenters();
        if (!plots.isEmpty()) {

            List<String> checkedLocs = new ArrayList<>();
            int x = plotWidth / 2;
            int z = plotWidth / 2;
            int direction = 1; // direction, 1 = north, 2 = east, 3 = south, 4 = west

            for (int i = 0; i < max; i++) {

                // check if current x and z are a plot center
                List<Map<String, String>> results = DatabaseQueries.getResults(
                        "plots",
                        "uuid, center_x, center_z",
                        " WHERE uuid='" + playerUUID + "'");

                if (results.isEmpty()) {
                    boolean isPlot = false;

                    for (String plotString : plots) {
                        String[] split = plotString.split(":");
                        int plotX = Integer.parseInt(split[0]);
                        int plotZ = Integer.parseInt(split[1]);

                        // check if the x and z coords exist in database
                        if (plotX == x && plotZ == z) {
                            isPlot = true;
                            break;
                        }
                    }
                    // if is a plot, keep going in algorithm
                    if (isPlot) {
                        switch (direction) {
                            // north
                            case 1:
                                if (checkedLocs.contains((x + plotWidth) + ":" + z))
                                    z -= plotWidth;
                                else
                                    x += plotWidth;
                            // east
                            case 2:
                                if (checkedLocs.contains(x + ":" + (z + plotWidth)))
                                    x += plotWidth;
                                else
                                    z += plotWidth;
                            // south
                            case 3:
                                if (checkedLocs.contains((x - plotWidth) + ":" + z))
                                    z += plotWidth;
                                else
                                    x -= plotWidth;
                            // west
                            case 4:
                                if (checkedLocs.contains(x + ":" + (z - plotWidth)))
                                    x -= plotWidth;
                                else
                                    z -= plotWidth;
                        }
                        if (direction % 4 > 0)
                            direction++;
                        else
                            direction = 1;

                        // add to checked locs
                        checkedLocs.add(x + ":" + z);
                    // create plot
                    } else {
                        return x + ":" + z;
                    }
                // already has plot
                } else {
                    return "hasPlot";
                }
            }
        // first plot
        } else {
            return (plotWidth / 2) + ":" + (plotWidth / 2);
        }
        return null;
    }
}