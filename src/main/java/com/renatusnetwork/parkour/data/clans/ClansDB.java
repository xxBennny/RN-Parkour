package com.renatusnetwork.parkour.data.clans;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;

import java.util.*;

public class ClansDB {

    static HashMap<String, Clan> getClans() {
        HashMap<String, Clan> clans = new HashMap<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "clans",
                "clan_id, clan_tag, owner_player_id, clan_level, clan_xp, total_gained_xp",
                ""
        );

        for (Map<String, String> result : results)
            clans.put(result.get("clan_tag"),
                    new Clan(
                            Integer.parseInt(result.get("clan_id")),
                            result.get("clan_tag"),
                            Integer.parseInt(result.get("owner_player_id")),
                            Integer.parseInt(result.get("clan_level")),
                            Integer.parseInt(result.get("clan_xp")),
                            Long.parseLong(result.get("total_gained_xp"))
                    )
            );

        Parkour.getPluginLogger().info("Clans Loaded: " + results.size());

        return clans;
    }

    static HashMap<Integer, List<ClanMember>> getMembers() {
        HashMap<Integer, List<ClanMember>> members = new HashMap<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "players",
                "player_id, uuid, player_name, clan_id",
                "WHERE clan_id > 0"
        );

        for (Map<String, String> result : results) {
            int clanID = Integer.parseInt(result.get("clan_id"));

            if (!members.containsKey(clanID))
                members.put(clanID, new ArrayList<>());

            members.get(clanID).add(new ClanMember(
                    Integer.parseInt(result.get("player_id")),
                    result.get("uuid"),
                    result.get("player_name")
            ));
        }

        Parkour.getPluginLogger().info("Clan Members Loaded: " + results.size());

        return members;
    }

    public static void newClan(Clan clan) {
        insertClan(clan);

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "clans",
                "clan_id",
                "WHERE clan_tag='" + clan.getTag() + "'"
        );

        for (Map<String, String> result : results)
            clan.setID(Integer.parseInt(result.get("clan_id")));

        PlayerStats owner = Parkour.getStatsManager().get(clan.getOwnerID());

        if (owner != null) {
            clan.addMember(new ClanMember(owner.getPlayerID(), owner.getUUID(), owner.getPlayer().getName()));
            owner.setClan(clan);
            updatePlayerClanID(owner);

            if (owner.getPlayer() != null && owner.getPlayer().isOnline())
                owner.getPlayer().sendMessage(Utils.translate("&7Successfully created your Clan called &3"
                        + clan.getTag()));


        }
    }

    public static void insertClan(Clan clan) {
        Parkour.getDatabaseManager().run(
                "INSERT INTO clans " +
                        "(clan_tag, owner_player_id, clan_level, clan_xp, total_gained_xp)" +
                        " VALUES " +
                        "('" +
                        clan.getTag() + "', " +
                        clan.getOwnerID() + ", " +
                        clan.getLevel() + ", " +
                        clan.getXP() + ", " +
                        clan.getTotalGainedXP() +
                        ")"
        );
    }

    public static void setClanXP(int clanXP, int clanID) {
        Parkour.getDatabaseManager().add("UPDATE clans SET " +
                "clan_xp=" + clanXP +
                " WHERE clan_id=" + clanID);
    }

    public static void setTotalGainedClanXP(long totalGainedClanXP, int clanID) {
        Parkour.getDatabaseManager().add("UPDATE clans SET " +
                "total_gained_xp=" + totalGainedClanXP +
                " WHERE clan_id=" + clanID);
    }

    public static void setClanLevel(int clanLevel, int clanID) {
        Parkour.getDatabaseManager().add("UPDATE clans SET " +
                "clan_level=" + clanLevel +
                " WHERE clan_id=" + clanID);
    }

    public static void removeClan(int clanID) {
        Parkour.getDatabaseManager().add(
                "DELETE FROM clans " +
                "WHERE clan_id=" + clanID);
    }

    public static void resetClanMember(String playerName) {
        Parkour.getDatabaseManager().add(
                "UPDATE players SET " +
                "clan_id=" + -1 +
                " WHERE player_name='" + playerName + "'");
    }

    public static Clan getClan(String playerName) {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                "players",
                "clan_id",
                "WHERE player_name='" + playerName + "'"
        );

        for (Map<String, String> completionResult : completionsResults) {

            int clanId = Integer.parseInt(completionResult.get("clan_id"));

            if (clanId > 0) {
                Clan clan = Parkour.getClansManager().get(clanId);

                if (clan != null)
                    return clan;
            }
        }
        return null;
    }

    public static void updatePlayerClanID(PlayerStats playerStats) {
        int clanID = -1;
        if (playerStats.getClan() != null)
            clanID = playerStats.getClan().getID();

        String query = "UPDATE players SET " +
                "clan_id=" + clanID +
                " WHERE player_id=" + playerStats.getPlayerID();

        Parkour.getDatabaseManager().add(query);
    }

    public static void updatePlayerClanID(String playerName, int clanID) {
        Parkour.getDatabaseManager().add(
                "UPDATE players SET clan_id=" + clanID + " WHERE player_name='" + playerName + "'"
        );
    }

    public static void updateClanTag(Clan clan) {
        String query = "UPDATE clans SET " +
                "clan_tag='" + clan.getTag() + "' " +
                "WHERE clan_id=" + clan.getID();

        Parkour.getDatabaseManager().add(query);
    }

    public static void updateClanOwnerID(Clan clan) {
        String query = "UPDATE clans SET " +
                "owner_player_id=" + clan.getOwnerID() +
                " WHERE clan_id=" + clan.getID();

        Parkour.getDatabaseManager().add(query);
    }
}