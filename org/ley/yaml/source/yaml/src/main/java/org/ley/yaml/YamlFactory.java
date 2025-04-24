package org.ley.yaml;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class YamlFactory {

    private static JavaPlugin plugin;
    private static final Map<String, YamlFile> files = new HashMap<>();
    private static String folderPath;

    public YamlFactory(JavaPlugin plugin, String folderPath) {
        YamlFactory.plugin = plugin;
        YamlFactory.folderPath = folderPath;
    }

    /**
     * Loads (or creates) a new YamlFile and stores it in the map.
     * @param folderPath Path to the folder relative to the plugin's data folder.
     * @param fileName Name of the YAML file.
     * @return The YamlFile instance.
     */
    public static YamlFile load(String folderPath, String fileName) {
        String key = folderPath + "/" + fileName;
        if (!files.containsKey(key)) {
            String fullFolderPath = plugin.getDataFolder().getAbsolutePath() + "/" + folderPath;
            YamlFile yamlFile = new YamlFile(plugin, fullFolderPath, fileName);
            files.put(key, yamlFile);
        }
        return files.get(key);
    }
    public static YamlFile load(String fileName) {return load(folderPath.toString(), fileName);}

    /**
     * Gets a loaded YamlFile from the map.
     * @param folderPath Path to the folder relative to the plugin's data folder.
     * @param fileName Name of the YAML file.
     * @return The YamlFile instance, or null if it hasn’t been loaded yet.
     */
    public static YamlFile get(String folderPath, String fileName) {
        String key = folderPath + "/" + fileName;
        return files.get(key);
    }
    public static YamlFile get(String fileName) {return get(folderPath.toString(), fileName);}

    /**
     * Saves all loaded YAML files.
     */
    public static void saveAll() {
        for (YamlFile file : files.values()) {
            file.save();
        }
    }

    /**
     * Reloads all loaded YAML files from disk.
     */
    public static void reloadAll() {
        for (YamlFile file : files.values()) {
            file.load();
        }
    }

    /**
     * Unloads a specific YAML file from memory (does not delete it from disk).
     * @param folderPath Path to the folder relative to the plugin's data folder.
     * @param fileName Name of the YAML file.
     */
    public static void unload(String folderPath, String fileName) {
        String key = folderPath + "/" + fileName;
        files.remove(key);
    }

    /**
     * Returns the plugin instance.
     * @return The given JavaPlugin instance.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
