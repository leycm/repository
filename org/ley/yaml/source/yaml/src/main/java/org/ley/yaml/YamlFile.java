package org.ley.yaml;

import java.util.List;

/**
 * Interface for YAML file operations with support for both Paper and BungeeCord platforms.
 */
public interface YamlFile {

    /**
     * Set a value in the YAML file.
     *
     * @param path The path where to store the value
     * @param object The object to store
     */
    void set(String path, Object object);

    /**
     * Get a value from the YAML file.
     *
     * @param path The path where the value is stored
     * @return The stored object or null if not found
     */
    Object get(String path);

    /**
     * Get a value from the YAML file with a default value if not found.
     *
     * @param path The path where the value is stored
     * @param defaultObject The default value to return if not found
     * @return The stored object or the default value if not found
     */
    Object get(String path, Object defaultObject);

    /**
     * Get a string from the YAML file.
     *
     * @param path The path where the string is stored
     * @return The stored string or null if not found
     */
    String getString(String path);

    /**
     * Get a string from the YAML file with a default value if not found.
     *
     * @param path The path where the string is stored
     * @param defaultValue The default value to return if not found
     * @return The stored string or the default value if not found
     */
    String getString(String path, String defaultValue);

    /**
     * Get an integer from the YAML file.
     *
     * @param path The path where the integer is stored
     * @return The stored integer or 0 if not found
     */
    int getInt(String path);

    /**
     * Get an integer from the YAML file with a default value if not found.
     *
     * @param path The path where the integer is stored
     * @param defaultValue The default value to return if not found
     * @return The stored integer or the default value if not found
     */
    int getInt(String path, int defaultValue);

    /**
     * Get a double from the YAML file.
     *
     * @param path The path where the double is stored
     * @return The stored double or 0.0 if not found
     */
    double getDouble(String path);

    /**
     * Get a double from the YAML file with a default value if not found.
     *
     * @param path The path where the double is stored
     * @param defaultValue The default value to return if not found
     * @return The stored double or the default value if not found
     */
    double getDouble(String path, double defaultValue);

    /**
     * Get a long from the YAML file.
     *
     * @param path The path where the long is stored
     * @return The stored long or 0L if not found
     */
    long getLong(String path);

    /**
     * Get a long from the YAML file with a default value if not found.
     *
     * @param path The path where the long is stored
     * @param defaultValue The default value to return if not found
     * @return The stored long or the default value if not found
     */
    long getLong(String path, long defaultValue);

    /**
     * Get a boolean from the YAML file.
     *
     * @param path The path where the boolean is stored
     * @return The stored boolean or false if not found
     */
    boolean getBoolean(String path);

    /**
     * Get a boolean from the YAML file with a default value if not found.
     *
     * @param path The path where the boolean is stored
     * @param defaultValue The default value to return if not found
     * @return The stored boolean or the default value if not found
     */
    boolean getBoolean(String path, boolean defaultValue);

    /**
     * Get a string list from the YAML file.
     *
     * @param path The path where the string list is stored
     * @return The stored string list or an empty list if not found
     */
    List<String> getStringList(String path);

    /**
     * Get an integer list from the YAML file.
     *
     * @param path The path where the integer list is stored
     * @return The stored integer list or an empty list if not found
     */
    List<Integer> getIntegerList(String path);

    /**
     * Get a double list from the YAML file.
     *
     * @param path The path where the double list is stored
     * @return The stored double list or an empty list if not found
     */
    List<Double> getDoubleList(String path);

    /**
     * Get a long list from the YAML file.
     *
     * @param path The path where the long list is stored
     * @return The stored long list or an empty list if not found
     */
    List<Long> getLongList(String path);

    /**
     * Get a boolean list from the YAML file.
     *
     * @param path The path where the boolean list is stored
     * @return The stored boolean list or an empty list if not found
     */
    List<Boolean> getBooleanList(String path);

    /**
     * Get a float list from the YAML file.
     *
     * @param path The path where the float list is stored
     * @return The stored float list or an empty list if not found
     */
    List<Float> getFloatList(String path);

    /**
     * Get a short list from the YAML file.
     *
     * @param path The path where the short list is stored
     * @return The stored short list or an empty list if not found
     */
    List<Short> getShortList(String path);

    /**
     * Get a character list from the YAML file.
     *
     * @param path The path where the character list is stored
     * @return The stored character list or an empty list if not found
     */
    List<Character> getCharacterList(String path);

    /**
     * Get a byte list from the YAML file.
     *
     * @param path The path where the byte list is stored
     * @return The stored byte list or an empty list if not found
     */
    List<Byte> getByteList(String path);

    /**
     * Get a configuration section from the YAML file.
     *
     * @param path The path where the configuration section is stored
     * @return The stored configuration section or null if not found
     */
    Object getConfigurationSection(String path);

    /**
     * Setup the YAML file.
     * Creates the file if it doesn't exist and loads it.
     */
    void setup();

    /**
     * Create the YAML file.
     */
    void create();

    /**
     * Load the YAML file.
     */
    void load();

    /**
     * Save the YAML file.
     */
    void save();

    /**
     * Check if a path exists in the YAML file.
     *
     * @param path The path to check
     * @return true if the path exists, false otherwise
     */
    boolean contains(String path);
}