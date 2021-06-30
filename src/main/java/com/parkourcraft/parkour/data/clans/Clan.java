package com.parkourcraft.parkour.data.clans;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    private int ID;
    private String tag;
    private int ownerID;
    private int clanLevel;
    private long clanXP;

    private List<ClanMember> members = new ArrayList<>(); // Does not include the owner
    private List<String> invitedUUIDs = new ArrayList<>();

    public Clan(int clanID, String clanTag, int clanOwnerID, int clanLevel, long clanXP) {
        this.ID = clanID;
        this.tag = clanTag;
        this.ownerID = clanOwnerID;
        this.clanLevel = clanLevel;
        this.clanXP = clanXP;
    }

    public void setID(int clanID) {
        this.ID = clanID;
    }

    public int getID() {
        return ID;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public int getLevel() { return clanLevel; }

    public long getXP() { return clanXP; }

    public void resetXP() {
        clanXP = 0;
    }

    public void setXP(long clanXP) { this.clanXP = clanXP; }

    public void addXP(long clanXP) { this.clanXP += clanXP; }

    public boolean isMaxLevel() {
        if (clanLevel >= ClansYAML.getMaxLevel())
            return true;
        return false;
    }

    public void setLevel(int level) {
        clanLevel = level;
    }

    public void setClanOwnerID(int clanOwnerID) {
        this.ownerID = clanOwnerID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public ClanMember getMemberFromUUID(String UUID) {
        for (ClanMember member : members)
            if (member.getUUID().equals(UUID))
                return member;

        return null;
    }

    public ClanMember getMemberFromName(String playerName) {
        for (ClanMember member : members)
            if (member.getPlayerName().equals(playerName))
                return member;

        return null;
    }

    public ClanMember getOwner() {
        for (ClanMember member : members)
            if (member.getPlayerID() == ownerID)
                return member;

        return null;
    }

    public void promoteOwner(String UUID) {
        ClanMember newOwner = getMemberFromUUID(UUID);

        if (newOwner != null)
            ownerID = newOwner.getPlayerID();
    }

    public boolean isMember(String UUID) {
        return getMemberFromUUID(UUID) != null;
    }

    public void addMember(ClanMember clanMember) {
        if (!isMember(clanMember.getUUID()))
            members.add(clanMember);
    }

    public void removeMemberFromUUID(String UUID) {
        ClanMember clanMember = getMemberFromUUID(UUID);

        if (clanMember != null)
            members.remove(clanMember);
    }

    public void removeMemberFromName(String playerName) {
        ClanMember clanMember = getMemberFromName(playerName);

        if (clanMember != null)
            members.remove(clanMember);
    }

    public void addInvite(String UUID) {
        if (!invitedUUIDs.contains(UUID))
            invitedUUIDs.add(UUID);
    }

    public void removeInvite(String UUID) {
        invitedUUIDs.remove(UUID);
    }

    public boolean isInvited(String UUID) {
        return invitedUUIDs.contains(UUID);
    }

    public List<ClanMember> getMembers() {
        return members;
    }
}