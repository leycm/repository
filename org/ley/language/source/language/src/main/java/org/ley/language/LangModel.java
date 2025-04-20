package org.ley.language;

import org.ley.language.types.Lang;

public class LangModel {
    //path -> url (api) or file path from server by default its ./lang
    public LangModel(String path) {

    }

    public static String getTranslationFor(String key, Lang lang) {
        return "";
    }

    public static String getTranslationFor(String key) {
        return getTranslationFor(key, Lang.get("en-en.json"));
    }
}
