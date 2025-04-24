package org.ley.language.types;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a language with its metadata and file path.
 * Manages caching of language instances for efficient retrieval.
 */
public class Lang {
    private static final String DEFAULT_LANG_PATH = "./lang";
    private static final Map<String, Lang> langCache = new HashMap<>();

    private String path;
    private String id;
    private String name;

    /**
     * Constructs a new language instance.
     * @param id The unique identifier for the language
     * @param path The file path to the language resource
     * @param name The display name of the language
     */
    public Lang(String id, String path, String name) {
        this.id = id;
        this.path = path;
        this.name = name;
    }

    /**
     * Retrieves a language instance from cache or creates a new one.
     * @param filePath Path to the language file (relative or absolute)
     * @return Lang instance or null if file doesn't exist
     */
    public static Lang get(String filePath) {
        if (langCache.containsKey(filePath)) {
            return langCache.get(filePath);
        }

        String fullPath;
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            fullPath = DEFAULT_LANG_PATH + File.separator + filePath;
        } else {
            fullPath = filePath;
        }

        File langFile = new File(fullPath);
        if (!langFile.exists() || !langFile.isFile()) {
            return null;
        }

        String langId = langFile.getName().replace(".json", "");
        String langName = langId.split("-")[0];

        Lang lang = new Lang(langId, fullPath, langName);
        langCache.put(filePath, lang);

        return lang;
    }

    /**
     * Clears all cached language instances.
     */
    public static void clearCache() {
        langCache.clear();
    }

    public String getId() {return id;}

    public String getPath() {return path;}

    public String getName() {return name;}
}