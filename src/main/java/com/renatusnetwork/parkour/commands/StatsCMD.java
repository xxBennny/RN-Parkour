package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.infinite.InfinitePKLBPosition;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.races.RaceLBPosition;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class StatsCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
            // infinite pk lb
            if (a.length == 1 && a[0].equalsIgnoreCase("infinite")) {

                if (!Parkour.getInfinitePKManager().getLeaderboard().isEmpty()) {
                    sender.sendMessage(Utils.translate("&5Infinite Parkour &7Leaderboard"));

                    int position = 1;
                    for (InfinitePKLBPosition lbPosition : Parkour.getInfinitePKManager().getLeaderboard()) {
                        if (lbPosition != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    position + " &5" +
                                    Utils.formatNumber(lbPosition.getScore()) + " &d" +
                                    lbPosition.getName()));
                        }
                        position++;
                    }

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());
                        sender.sendMessage(Utils.translate("&7Your best &d" + Utils.formatNumber(playerStats.getInfinitePKScore())));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cInfinite Parkour lb not loaded or no lb positions"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("levels")) {

                LinkedHashSet<Level> globalLevelCompletionsLB = Parkour.getLevelManager().getGlobalLevelCompletionsLB();

                if (!globalLevelCompletionsLB.isEmpty()) {

                    sender.sendMessage(Utils.translate("&4Level Completions &7Leaderboard"));

                    int lbPositionNum = 1;
                    for (Level lbPosition : globalLevelCompletionsLB) {

                        if (lbPosition != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPositionNum + " &4" +
                                    Utils.shortStyleNumber(lbPosition.getTotalCompletionsCount()) + " &c" +
                                    lbPosition.getFormattedTitle()));
                            lbPositionNum++;
                        }
                    }
                    sender.sendMessage(Utils.translate("&7Global Completions &c" + Utils.formatNumber(Parkour.getLevelManager().getTotalLevelCompletions())));
                } else {
                    sender.sendMessage(Utils.translate("&cLevels lb not loaded or no lb positions"));
                }
            // players
            } else if (a.length == 1 && a[0].equalsIgnoreCase("players")) {

                LinkedHashMap<String, Integer> globalPersonalCompletionsLB = Parkour.getStatsManager().getGlobalPersonalCompletionsLB();

                if (!globalPersonalCompletionsLB.isEmpty()) {

                    sender.sendMessage(Utils.translate("&3Player Completions &7Leaderboard"));

                    int lbPositionNum = 1;
                    for (Map.Entry<String, Integer> entry : globalPersonalCompletionsLB.entrySet()) {

                        if (entry != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPositionNum + " &3" +
                                    Utils.shortStyleNumber(entry.getValue()) + " &b" +
                                    entry.getKey()));
                            lbPositionNum++;
                        }
                    }
                    // if player, send personal total
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        sender.sendMessage(Utils.translate("&7Your total &b" + Utils.formatNumber(
                                Parkour.getStatsManager().get(player).getTotalLevelCompletions())));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cPlayers lb not loaded or no lb positions"));
                }
            // clans lb
            } else if (a.length == 1 && a[0].equalsIgnoreCase("clans")) {

                LinkedHashSet<Clan> clansLB = Parkour.getClansManager().getLeaderboard();

                if (!clansLB.isEmpty()) {

                    sender.sendMessage(Utils.translate("&6Clan Total XP &7Leaderboard"));
                    int lbPositionNum = 1;
                    for (Clan clan : clansLB) {

                        if (clan != null && clan.getOwner().getPlayerName() != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPositionNum + " &6" +
                                    Utils.shortStyleNumber(clan.getTotalGainedXP()) + " &e" +
                                    clan.getTag() + " &6(" + clan.getOwner().getPlayerName() + ")"));
                            lbPositionNum++;
                        }
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cClans lb not loaded or no lb positions"));
                }
            // top rated lb
            } else if (a.length == 1 && a[0].equalsIgnoreCase("toprated")) {

                LinkedHashSet<Level> topRatedLB = Parkour.getLevelManager().getTopRatedLevelsLB();

                if (!topRatedLB.isEmpty()) {

                    sender.sendMessage(Utils.translate("&9Rated Levels &7Leaderboard"));

                    int lbPositionNum = 1;
                    for (Level level : topRatedLB) {

                        if (level != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPositionNum + " &9" +
                                    level.getRating() + " &1" +
                                    level.getFormattedTitle()));
                            lbPositionNum++;
                        }
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cTop Rated lb not loaded or no lb positions"));
                }
            // race lb
            } else if (a.length == 1 && a[0].equalsIgnoreCase("races")) {

                LinkedHashSet<RaceLBPosition> leaderboard = Parkour.getRaceManager().getLeaderboard();

                if (!leaderboard.isEmpty()) {

                    sender.sendMessage(Utils.translate("&8Races &7Leaderboard"));

                    int position = 1;
                    for (RaceLBPosition lbPosition : leaderboard) {
                        if (lbPosition != null) {

                            // just for my sanity of proper grammar
                            String winMsg = "win";
                            if (lbPosition.getWins() > 1)
                                winMsg += "s";

                            sender.sendMessage(Utils.translate(" &7" +
                                    position + " &8" +
                                    lbPosition.getWins() + " " + winMsg + " &7" +
                                    lbPosition.getName() + " &8(" +
                                    lbPosition.getWinRate() + ")"));
                        }
                        position++;
                    }

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());
                        sender.sendMessage(Utils.translate("&7Your Wins/Win Rate &8" +
                                Utils.formatNumber(playerStats.getRaceWins()) + "&7/&8" +
                                playerStats.getRaceWinRate()));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cRace lb not loaded or no lb positions"));
                }
            // level lb
            } else {

                // allow ability to get from title or name
                String levelName = a[0].toLowerCase();
                if (a.length >= 1) {
                    String[] split = Arrays.copyOfRange(a, 0, a.length);
                    levelName = String.join(" ", split);
                }

                // if it does not get it from name, then attempt to get it from title
                Level level = Parkour.getLevelManager().get(levelName);
                if (level == null)
                    level = Parkour.getLevelManager().getFromTitle(levelName);

                // then check if it is still null
                if (level != null) {

                    sender.sendMessage(Utils.translate(level.getFormattedTitle() + " &7Leaderboard"));
                    List<LevelCompletion> completions = level.getLeaderboard();

                    if (completions.size() > 0)
                        for (int i = 0; i <= completions.size() - 1; i++) {
                            LevelCompletion levelCompletion = completions.get(i);
                            int rank = i + 1;
                            sender.sendMessage(Utils.translate(" &7" + rank + " &2" +
                                    (((double) levelCompletion.getCompletionTimeElapsed()) / 1000) + "s &a" +
                                    levelCompletion.getPlayerName()));
                        }
                    else
                        sender.sendMessage(Utils.translate("&cNo timed completions to display"));

                    int totalCompletionsCount = level.getTotalCompletionsCount();
                    String outOfMessage = Utils.translate("&7Out of &2" + totalCompletionsCount);

                    sender.sendMessage(outOfMessage);
                } else {
                    sender.sendMessage(Utils.translate("&7No level named '&c" + levelName + "&7' exists"));
                }
            }
        } else {
            sender.sendMessage(Utils.translate("&6/stats <levelName>  &7Gets level's Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats infinite  &7Gets Infinite Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats races  &7Gets Races Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats toprated  &7Gets Top Rated Levels Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats clans  &7Gets Clan XP Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats players  &7Gets Players Leaderboard"));
            sender.sendMessage(Utils.translate("&6/stats levels  &7Gets Levels Leaderboard"));
        }
        return true;
    }
}