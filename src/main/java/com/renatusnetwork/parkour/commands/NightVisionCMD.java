package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {

            Player player = (Player) sender;
            StatsManager statsManager = Parkour.getStatsManager();

            PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0);

            if (a.length == 0) {
                PlayerStats playerStats = statsManager.get(player);

                if (!playerStats.hasNVStatus()) { // enable

                    playerStats.setNVStatus(true);
                    player.addPotionEffect(nightVision);
                    sender.sendMessage(Utils.translate("&aYou have enabled Night Vision"));
                } else { // disable

                    // if !(in level and level is ascendance level) remove night vision
                    if (!(playerStats.inLevel() && playerStats.getLevel().isAscendanceLevel()))
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                    playerStats.setNVStatus(false);
                    player.sendMessage(Utils.translate("&cYou have disabled Night Vision"));
                }
                // update db
                StatsDB.updatePlayerNightVision(playerStats);
            }
        }
        return true;
    }
}
