package com.parkourcraft.parkour.data.clans;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ClansManager {

    private HashMap<String, Clan> clans = new HashMap<>();
    private LinkedHashSet<Clan> clansLeaderboard = new LinkedHashSet<>(Parkour.getSettingsManager().max_clans_leaderboard_size);
    private HashMap<String, Clan> clanChat = new HashMap<>();
    private Set<String> chatSpy = new HashSet<>();

    public ClansManager(Plugin plugin) {
        load();

        startScheduler(plugin);
    }

    private void load() {
        clans = ClansDB.getClans();

        HashMap<Integer, List<ClanMember>> members = ClansDB.getMembers();

        syncMembers(members);
    }

    private void syncMembers(Map<Integer, List<ClanMember>> members) {
        for (Map.Entry<Integer, List<ClanMember>> entry : members.entrySet()) {
            Clan clan = get(entry.getKey());

            if (clan != null)
                for (ClanMember member : entry.getValue())
                    clan.addMember(member);
        }
    }

    public void add(Clan clan) {
        clans.put(clan.getTag(), clan);
    }

    public void addMember(int clanID, ClanMember clanMember) {
        Clan clan = get(clanID);

        if (clan != null)
            clan.addMember(clanMember);
    }

    public void removeClan(int clanID) {
        clans.remove(clanID);
    }

    public Clan get(int clanID) {
        for (Clan clan : clans.values())
            if (clan.getID() == clanID)
                return clan;

        return null;
    }

    public Clan get(String clanTag) {
        return clans.get(clanTag);
    }

    public LinkedHashSet<Clan> getLeaderboard() { return clansLeaderboard; }

    public void loadLeaderboard() {
        try {

            Clan highestXPClan = null;
            Set<Clan> alreadyAddedClans = new HashSet<>();
            LinkedHashSet<Clan> temporaryClanLB = new LinkedHashSet<>();
            int lbSize = 0;

            while (Parkour.getSettingsManager().max_clans_leaderboard_size > lbSize) {
                // loop through and make sure they are not already added, and higher than previous
                for (Clan clan : clans.values())
                    if (!alreadyAddedClans.contains(clan) &&
                        (highestXPClan == null || clan.getTotalGainedXP() > highestXPClan.getTotalGainedXP()))
                        highestXPClan = clan;

                temporaryClanLB.add(highestXPClan);
                alreadyAddedClans.add(highestXPClan);
                highestXPClan = null;
                lbSize++;
            }
            // clear and then add all from temporary (fast swap)
            clansLeaderboard.clear();
            clansLeaderboard.addAll(temporaryClanLB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSplitClanReward(Clan clan, Player player, Level level) {

        double percentage = (double) clan.getLevel() / 100;
        double splitAmountPerMember = level.getReward() * percentage;

        for (ClanMember clanMember : clan.getMembers()) {
            // make sure it is not given to the completioner
            if (!clanMember.getPlayerName().equalsIgnoreCase(player.getName())) {
                // check if they are online
                if (Bukkit.getPlayer(UUID.fromString(clanMember.getUUID())) != null) {

                    Player onlineMember = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                    Parkour.getEconomy().depositPlayer(onlineMember, splitAmountPerMember);

                    onlineMember.sendMessage(Utils.translate("&6" + player.getName() + " &ehas completed &6" +
                            level.getFormattedTitle() + " &eand you received &6" + (percentage * 100) + "%" +
                            " &eof the reward! &6($" + (int) splitAmountPerMember + ")"));
                } else {
                    OfflinePlayer offlineMember = Bukkit.getOfflinePlayer(UUID.fromString(clanMember.getUUID()));
                    Parkour.getEconomy().depositPlayer(offlineMember, splitAmountPerMember);
                }
            }
        }
    }

    public void doClanXPCalc(Clan clan, Player player, Level level) {
        int min = Parkour.getSettingsManager().clan_calc_percent_min;
        int max = Parkour.getSettingsManager().clan_calc_percent_max;

        // get random percent
        double percent = ThreadLocalRandom.current().nextInt(min, max) / 100.0;
        int clanXP = (int) (level.getReward() * percent);
        int totalXP = clanXP + clan.getXP();

        // if max level, keep calculating xp
        if (clan.isMaxLevel()) {
            clan.addXP(clanXP);
            ClansDB.setClanXP(totalXP, clan.getID());
            sendMessageToMembers(clan, "&6" + player.getName() + " &ehas gained &6&l" +
                                Utils.formatNumber(clanXP) + " &eXP for your clan!" +
                                " Total XP &6&l" + Utils.shortStyleNumber(clan.getTotalGainedXP()), null);

        // level them up
        } else if (totalXP > ClansYAML.getLevelUpPrice(clan)) {

            // left over after level up
            int clanXPOverflow = totalXP - ClansYAML.getLevelUpPrice(clan);
            int newLevel = clan.getLevel() + 1;

            // this is the section that will determine if they will skip any levels
            for (int i = clan.getLevel(); i <= ClansYAML.getMaxLevel(); i++) {
                // this means they are still above the next level amount
                if (clanXPOverflow >= ClansYAML.getLevelUpPrice(newLevel)) {

                    // remove from overflow and add +1 level
                    clanXPOverflow -= ClansYAML.getLevelUpPrice(newLevel);
                    newLevel++;
                } else {
                    break;
                }
            }
            clan.setLevel(newLevel);
            sendMessageToMembers(clan, "&eYour clan has leveled up to &6&lLevel " + newLevel, null);

            // add rest of xp after leveling up
            ClansDB.setClanLevel(newLevel, clan.getID());
            ClansDB.setClanXP(clanXPOverflow, clan.getID());
            clan.setXP(clanXPOverflow);

        // add xp to clan
        } else {

            // otherwise add xp to cache and database
            clan.addXP(clanXP);
            ClansDB.setClanXP(totalXP, clan.getID());

            long clanXPNeeded = ClansYAML.getLevelUpPrice(clan) - clan.getXP();

            sendMessageToMembers(clan, "&6" + player.getName() + " &ehas gained &6&l" +
                    Utils.formatNumber(clanXP) + " &eXP for your clan! &c(XP Needed to Level Up - &4" +
                    Utils.formatNumber(clanXPNeeded) + "&c)", null);
        }
        // update total gained xp
        ClansDB.setTotalGainedClanXP(clan.getTotalGainedXP() + clanXP, clan.getID());
        clan.setTotalGainedXP(clan.getTotalGainedXP() + clanXP);
    }

    public void deleteClan(int clanID, boolean messageMembers) {
        Clan clan = get(clanID);

        if (clan != null) {
            // iterate through existing clan members to reset/remove their data
            for (ClanMember clanMember : clan.getMembers()) {

                // reset clan member in database
                ClansDB.resetClanMember(clanMember.getPlayerName());

                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                if (clanPlayer != null) {

                    if (messageMembers) {
                        clanPlayer.sendMessage(Utils.translate("&6&l" + clan.getOwner().getPlayerName() +
                                " &ehas disbanded your &6&lClan &6" + clan.getTag()));
                    }

                    // reset data on the players
                    Parkour.getStatsManager().get(clanPlayer).resetClan();
                }
            }
            // remove from database and list
            ClansDB.removeClan(clanID);
            clans.remove(clan.getTag());
        }
    }

    public void sendMessageToMembers(Clan clan, String msg, String dontSendTo) {
        for (ClanMember clanMember : clan.getMembers()) {
            // make sure they are online
            Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

            if (clanPlayer != null)
                if (dontSendTo != null && clanPlayer.getName().equalsIgnoreCase(dontSendTo))
                    continue;
                else
                    clanPlayer.sendMessage(Utils.translate(msg));
        }
    }

    private void syncNewClans() {
        for (Clan clan : clans.values())
            if (clan.getID() == -1)
                ClansDB.newClan(clan);
    }

    private void startScheduler(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                syncNewClans();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 5);

        // load clans leaderboard in async every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 10, 20 * 180);
    }

    public boolean isInClanChat(String playerName) {
        return clanChat.containsKey(playerName);
    }

    public void toggleClanChat(String playerName, Clan clan) {
        if (isInClanChat(playerName) || clan == null)
            clanChat.remove(playerName);
        else
            clanChat.put(playerName, clan);
    }

    public boolean isInChatSpy(String playerName) {
        return chatSpy.contains(playerName);
    }

    public void toggleChatSpy(String playerName, boolean disconnected) {
        if (isInChatSpy(playerName) || disconnected)
            chatSpy.remove(playerName);
        else
            chatSpy.add(playerName);
    }

    public HashMap<String, Clan> getClans() { return clans; }

    public HashMap<String, Clan> getClanChatMap() { return clanChat; }

    public Set<String> getChatSpyMap() { return chatSpy; }
}
