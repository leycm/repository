package org.ley.yaml;

import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for managing YAML configuration files in both Bukkit and BungeeCord environments.
 * This class provides static methods to load, access, and manage YAML files.
 *
 * <p>Before using the factory, you must initialize it with either a Bukkit or BungeeCord plugin instance.</p>
 *
 * <p>Example usage (Bukkit):
 * <pre>{@code
 * YamlFactory.initialize(plugin);
 * YamlFile config = YamlFactory.load("config.yml");
 * }</pre>
 * </p>
 *
 * <p>Example usage (BungeeCord):
 * <pre>{@code
 * YamlFactory.initialize(plugin);
 * YamlFile config = YamlFactory.load("config.yml");
 * }</pre>
 * </p>
 */
public final class YamlFactory {
    private static JavaPlugin bukkitPlugin;
    private static Plugin bungeePlugin;
    private static final Map<String, YamlFile> files = new HashMap<>();
    private static String defaultFolderPath = "";

    private YamlFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Initializes the factory with a Bukkit plugin instance.
     * @param plugin The Bukkit JavaPlugin instance
     * @throws IllegalStateException if the factory is already initialized
     */
    public static void initialize(JavaPlugin plugin) {
        if (bukkitPlugin != null || bungeePlugin != null) {
            throw new IllegalStateException("YamlFactory is already initialized");
        }
        bukkitPlugin = plugin;
    }

    /**
     * Initializes the factory with a BungeeCord plugin instance.
     * @param plugin The BungeeCord Plugin instance
     * @throws IllegalStateException if the factory is already initialized
     */
    public static void initialize(Plugin plugin) {
        if (bukkitPlugin != null || bungeePlugin != null) {
            throw new IllegalStateException("YamlFactory is already initialized");
        }
        bungeePlugin = plugin;
    }

    /**
     * Sets the default folder path for YAML files.
     * @param folderPath The default path relative to the plugin's data folder
     */
    public static void setDefaultFolderPath(String folderPath) {
        defaultFolderPath = folderPath;
    }

    /**
     * Loads (or creates) a new YamlFile and stores it in the map.
     * @param folderPath Path to the folder relative to the plugin's data folder
     * @param fileName Name of the YAML file
     * @return The YamlFile instance
     * @throws IllegalStateException if the factory is not initialized
     */
    public static YamlFile load(String folderPath, String fileName) {
        if (bukkitPlugin == null && bungeePlugin == null) {
            throw new IllegalStateException("YamlFactory not initialized. Call initialize() first");
        }

        String key = folderPath + "/" + fileName;
        if (!files.containsKey(key)) {
            YamlFile yamlFile;
            if (bukkitPlugin != null)
                yamlFile = new BukkitFile(bukkitPlugin, folderPath, fileName);
            else
                yamlFile = new BungeeFile(bungeePlugin, folderPath, fileName);

            files.put(key, yamlFile);
        }
        return files.get(key);
    }

    /**
     * Loads a YAML file using the default folder path.
     * @param fileName Name of the YAML file
     * @return The YamlFile instance
     * @see #load(String, String)
     */
    public static YamlFile load(String fileName) {
        return load(defaultFolderPath, fileName);
    }

    /**
     * Gets a loaded YamlFile from the map.
     * @param folderPath Path to the folder relative to the plugin's data folder
     * @param fileName Name of the YAML file
     * @return The YamlFile instance, or null if it hasn't been loaded yet
     */
    public static YamlFile get(String folderPath, String fileName) {
        String key = folderPath + "/" + fileName;
        return files.get(key);
    }

    /**
     * Gets a loaded YamlFile from the map using the default folder path.
     * @param fileName Name of the YAML file
     * @return The YamlFile instance, or null if it hasn't been loaded yet
     */
    public static YamlFile get(String fileName) {
        return get(defaultFolderPath, fileName);
    }

    /**
     * Saves all loaded YAML files to disk.
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
     * @param folderPath Path to the folder relative to the plugin's data folder
     * @param fileName Name of the YAML file
     * @return The unloaded YamlFile instance, or null if it wasn't loaded
     */
    public static YamlFile unload(String folderPath, String fileName) {
        String key = folderPath + "/" + fileName;
        return files.remove(key);
    }

    /**
     * Unloads a YAML file from memory using the default folder path.
     * @param fileName Name of the YAML file
     * @return The unloaded YamlFile instance, or null if it wasn't loaded
     */
    public static YamlFile unload(String fileName) {
        return unload(defaultFolderPath, fileName);
    }

    /**
     * Gets the Bukkit plugin instance if running in Bukkit environment.
     * @return The Bukkit JavaPlugin instance, or null if running in BungeeCord
     */
    public static JavaPlugin getBukkitPlugin() {
        return bukkitPlugin;
    }

    /**
     * Gets the BungeeCord plugin instance if running in BungeeCord environment.
     * @return The BungeeCord Plugin instance, or null if running in Bukkit
     */
    public static Plugin getBungeePlugin() {
        return bungeePlugin;
    }
}