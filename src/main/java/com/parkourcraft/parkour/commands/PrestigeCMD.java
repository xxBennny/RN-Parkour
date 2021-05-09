package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.rank.RanksManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        RanksManager rankManager = Parkour.getRanksManager();

        if (a.length == 0) {
            // this means they are max rank
            if (rankManager.isMaxRank(playerStats.getRank())) {
                rankManager.doPrestige(player);
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this yet!" +
                                                        " You need to be Rank &4" + rankManager.getMaxRank().getRankTitle()));
            }
        }
        return false;
    }
}
