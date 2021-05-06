package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private String name;
    private String title;
    private int reward = 0;
    private Location startLocation;
    private Location respawnLocation;
    private String message;
    private int maxCompletions;
    private boolean broadcastCompletion;
    private List<String> requiredLevels;
    private int ID = -1;
    private int scoreModifier = 1;
    private boolean isRankUpLevel = false;
    private List<PotionEffect> potionEffects = new ArrayList<>();

    private boolean raceLevel = false;

    private boolean eventLevel = false;
    private EventType eventType;

    private Location raceLocation1 = null;
    private Location raceLocation2 = null;

    private int totalCompletionsCount = -1;
    private List<LevelCompletion> leaderboardCache = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    public Level(String levelName) {
        this.name = levelName;
        load();
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage(PlayerStats playerStats) {
        if (message != null) {
            String returnMessage = Utils.translate(message);

            returnMessage = returnMessage.replace("%title%", getFormattedTitle());
            returnMessage = returnMessage.replace("%reward%", Utils.formatNumber(reward));
            returnMessage = returnMessage.replace(
                    "%completions%",
                    Integer.toString(playerStats.getLevelCompletionsCount(name))
            );

            return returnMessage;
        }

        return "";
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public boolean getBroadcastCompletion() {
        return broadcastCompletion;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public List<String> getCommands() { return commands; }

    public boolean hasCommands() {
        if (!commands.isEmpty())
            return true;
        return false;
    }

    public void setTotalCompletionsCount(int count) {
        totalCompletionsCount = count;
    }

    public int getTotalCompletionsCount() {
        return totalCompletionsCount;
    }

    public boolean isRankUpLevel() {
        return isRankUpLevel;
    }

    public boolean isEventLevel() { return eventLevel; }

    public EventType getEventType() {
        EventType event = null;

        if (eventLevel)
            event = eventType;

        return event;
    }

    public void setRankUpLevel(boolean isRankupLevel) {
        this.isRankUpLevel = isRankupLevel;
    }

    public void sortNewCompletion(LevelCompletion levelCompletion) {
        List<LevelCompletion> newLeaderboard = new ArrayList<>(leaderboardCache);

        if (newLeaderboard.size() > 0) {
            newLeaderboard.add(levelCompletion);

            // Bubble sort
            for (int outer = 0; outer < newLeaderboard.size() - 1; outer++) {
                for (int inner = 0; inner < newLeaderboard.size() - outer - 1; inner++) {
                    if (newLeaderboard.get(inner).getCompletionTimeElapsed()
                            > newLeaderboard.get(inner + 1).getCompletionTimeElapsed()) {
                        LevelCompletion tempCompletion = newLeaderboard.get(inner);

                        newLeaderboard.set(inner, newLeaderboard.get(inner + 1));
                        newLeaderboard.set(inner + 1, tempCompletion);
                    }
                }
            }

            // Trimming potential #11 datapoint
            if (newLeaderboard.size() > 10)
                newLeaderboard.remove(10);
        } else {
            newLeaderboard.add(0, levelCompletion);
        }

        leaderboardCache = newLeaderboard;
    }

    public void addCompletion(Player player, LevelCompletion levelCompletion, Level level) {
        if (totalCompletionsCount < 0)
            totalCompletionsCount = 0;

        totalCompletionsCount += 1;

        if (leaderboardCache.isEmpty()) {
            leaderboardCache.add(levelCompletion);
            return;
        }

        if (levelCompletion.getCompletionTimeElapsed() <= 0)
            return;

        // Compare completion against scoreboard
        if (leaderboardCache.get(leaderboardCache.size() - 1).getCompletionTimeElapsed()
                > levelCompletion.getCompletionTimeElapsed()) {

            LevelCompletion completionToRemove = null;
            for (LevelCompletion completion : leaderboardCache) {
                if (completion.getPlayerName().equalsIgnoreCase(player.getName()))
                    completionToRemove = completion;
            }
            if (completionToRemove != null)
            leaderboardCache.remove(completionToRemove);

            sortNewCompletion(levelCompletion);
        }
    }

    public void setScoreModifier(int scoreModifier) {
        this.scoreModifier = scoreModifier;
    }

    public int getScoreModifier() {
        return scoreModifier;
    }

    private void load() {
        if (LevelsYAML.exists(name)) {

            if (LevelsYAML.isSet(name, "title"))
                title = LevelsYAML.getTitle(name);
            else
                title = name;

            String startLocationName = name + "-spawn";
            if (Parkour.getLocationManager().exists(startLocationName))
                startLocation = Parkour.getLocationManager().get(startLocationName);
            else
                startLocation = Parkour.getLocationManager().get("spawn");

            String respawnLocationName = name + "-completion";
            if (Parkour.getLocationManager().exists(respawnLocationName))
                respawnLocation = Parkour.getLocationManager().get(respawnLocationName);
            else
                respawnLocation = Parkour.getLocationManager().get("spawn");

            if (LevelsYAML.isSet(name, "message"))
                message = LevelsYAML.getMessage(name);
            else
                message = Parkour.getSettingsManager().levels_message_completion;

            // this acts as a boolean for races
            if (LevelsYAML.isSection(name, "race")) {
                // this checks if player1 and player2 has locations
                raceLevel = true;

                if (LevelsYAML.isSet(name, "race.player1-loc") &&
                    LevelsYAML.isSet(name, "race.player2-loc")) {

                    raceLocation1 = LevelsYAML.getPlayerRaceLocation("player1", name);
                    raceLocation2 = LevelsYAML.getPlayerRaceLocation("player2", name);
                }
            }

            if (LevelsYAML.isSet(name, "event")) {
                eventLevel = true;
                eventType = LevelsYAML.getEventType(name);
            }
            // if level is a rankUpLevel
            if (LevelsYAML.isSet(name, "rankup-level"))
                isRankUpLevel = LevelsYAML.getRankUpLevelSwitch(name);

            maxCompletions = LevelsYAML.getMaxCompletions(name);
            broadcastCompletion = LevelsYAML.getBroadcastSetting(name);
            requiredLevels = LevelsYAML.getRequiredLevels(name);
            potionEffects = LevelsYAML.getPotionEffects(name);
            commands = LevelsYAML.getCommands(name);
        }
    }

    public void setLeaderboardCache(List<LevelCompletion> levelCompletions) {
        leaderboardCache = levelCompletions;
    }

    public List<LevelCompletion> getLeaderboard() {
        return leaderboardCache;
    }

    public List<String> getRequiredLevels() {
        return requiredLevels;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public Location getRaceLocation1() {
        return raceLocation1;
    }

    public Location getRaceLocation2() {
        return raceLocation2;
    }

    public boolean isRaceLevel() {
        return raceLevel;
    }

    public boolean hasValidRaceLocations() {
        if (LevelsYAML.isSet(name, "race.player1-loc") && LevelsYAML.isSet(name, "race.player2-loc"))
            return true;
        return false;
    }

    public boolean hasRequiredLevels(PlayerStats playerStats) {
        for (String levelName : requiredLevels)
            if (playerStats.getLevelCompletionsCount(levelName) < 1)
                return false;

        return true;
    }
}