package com.parkourcraft.Parkour.utils.storage;


import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Levels_YAML {

    private static FileConfiguration levels = FileManager.getFileConfig("levels");

    public static void commitChange(String levelName) {
        FileManager.save("levels");
        LevelManager.loadLevel(levelName);
    }

    public static void create(String levelName) {
        if (!levels.isSet(levelName))
            levels.set(levelName + ".reward", 0);

        commitChange(levelName);
    }

    public static void delete(String levelName) {
        if (levelExists(levelName))
            levels.set(levelName, null);

        commitChange(levelName);

    }

    public static boolean levelExists(String levelName) {
        if (levels.isSet(levelName))
            return true;
        return false;
    }

    public static List<String> getLevelNames() {
        List<String> levelNames = new ArrayList<String>(levels.getKeys(false));

        return levelNames;
    }

    public static void setTitle(String levelName, String title) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".title", title);

        commitChange(levelName);
    }

    public static String getTitle(String levelName) {
        if (levels.isSet(levelName + ".title"))
            return levels.getString(levelName + ".title");
        return "";
    }

    public static void setReward(String levelName, int reward) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".reward", reward);

        commitChange(levelName);
    }

    public static int getReward(String levelName) {
        if (levels.isSet(levelName + ".reward"))
            return levels.getInt(levelName + ".reward");
        return 0;
    }

    public static void setStartLocationName(String levelName, String locationName) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".start_location", locationName);

        commitChange(levelName);
    }

    public static void setRespawnLocationName(String levelName, String locationName) {
        if (locationName.equalsIgnoreCase("default"))
            levels.set(levelName, null);
        else if (levels.isSet(levelName))
            levels.set(levelName + ".respawn_location", locationName);

        commitChange(levelName);
    }

    public static void setMessage(String levelName, String message) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".message", message);

        commitChange(levelName);
    }

    public static String getMessage(String levelName) {
        if (levels.isSet(levelName + ".message"))
            return levels.getString(levelName + ".message");
        return "";
    }

    public static void setMaxCompletions(String levelName, int maxCompletions) {
        if (levels.isSet(levelName)) {
            if (maxCompletions == -1)
                levels.set(levelName + ".max_completions", null);
            else
                levels.set(levelName + ".max_completions", maxCompletions);
        }

        commitChange(levelName);
    }

    public static int getMaxCompletions(String levelName) {
        if (levels.isSet(levelName + ".max_completions"))
            return levels.getInt(levelName + ".max_completions");
        return 0;
    }

}
