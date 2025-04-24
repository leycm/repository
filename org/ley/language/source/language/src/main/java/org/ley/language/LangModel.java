package org.ley.language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ley.language.types.Lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Language model for handling translations from JSON files.
 * Supports caching loaded language files for better performance.
 */
public class LangModel {
    private static final String DEFAULT_LANG_DIR = "./lang";
    private String langPath;
    private static final Map<String, JsonNode> langCache = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a LangModel with specified path or default directory.
     * @param path Custom path to language files directory or API URL
     */
    public LangModel(String path) {
        this.langPath = path != null ? path : DEFAULT_LANG_DIR;
    }

    /**
     * Gets translation for a key using default English language.
     * @param key The translation key to look up
     * @return Translated string or the key if not found
     */
    public static String getTranslationFor(String key) {
        return getTranslationFromJson("messages", key, Lang.get("en-en.json"));
    }

    /**
     * Gets translation for a key using specified language.
     * @param key The translation key to look up
     * @param lang The language to use for translation
     * @return Translated string or the key if not found
     */
    public static String getTranslationFor(String key, Lang lang) {
        return getTranslationFromJson("messages", key, lang);
    }

    /**
     * Gets translation for a key within a specific path using default English.
     * @param path The JSON path where the key is located
     * @param key The translation key to look up
     * @return Translated string or the key if not found
     */
    public static String getTranslationFor(String path, String key) {
        return getTranslationFromJson(path, key, Lang.get("en-en.json"));
    }

    /**
     * Gets translation for a key within a specific path using specified language.
     * @param path The JSON path where the key is located
     * @param key The translation key to look up
     * @param lang The language to use for translation
     * @return Translated string or the key if not found
     */
    public static String getTranslationFor(String path, String key, Lang lang) {
        return getTranslationFromJson(path, key, lang);
    }

    /**
     * Internal method to retrieve translation from JSON structure.
     * @param path The JSON path to search in
     * @param key The translation key to find
     * @param lang The language object containing translations
     * @return Translated string or the key if not found
     */
    private static String getTranslationFromJson(String path, String key, Lang lang) {
        if (lang == null) {
            return key;
        }

        try {
            JsonNode rootNode = loadLanguageFile(lang);
            if (rootNode == null) {
                return key;
            }

            JsonNode pathNode = rootNode;
            if (path != null && !path.isEmpty()) {
                String[] pathParts = path.split("\\.");
                for (String part : pathParts) {
                    pathNode = pathNode.path(part);
                    if (pathNode.isMissingNode()) {
                        return key;
                    }
                }
            }

            JsonNode valueNode = pathNode.path(key);
            if (valueNode.isMissingNode()) {
                return key;
            }

            if (valueNode.isTextual()) {
                return valueNode.asText();
            } else {
                return key;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }

    /**
     * Loads language file from disk or cache.
     * @param lang The language to load
     * @return JsonNode containing all translations for the language
     * @throws IOException If there's an error reading the file
     */
    private static JsonNode loadLanguageFile(Lang lang) throws IOException {
        String langId = lang.getId();
        String langPath = lang.getPath();

        if (langCache.containsKey(langId)) {
            return langCache.get(langId);
        }

        File langFile = new File(langPath);
        if (!langFile.exists()) {
            return null;
        }

        String content = new String(Files.readAllBytes(Paths.get(langPath)));
        JsonNode rootNode = mapper.readTree(content);
        langCache.put(langId, rootNode);
        return rootNode;
    }

    /**
     * Clears the language file cache.
     */
    public static void clearCache() {
        langCache.clear();
    }
}