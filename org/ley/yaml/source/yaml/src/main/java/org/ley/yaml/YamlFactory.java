package org.ley.yaml;

import org.apache.maven.model.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class YamlFactory {
    private static JavaPlugin plugin;
    private static Map<String, YamlFile> files;

    public YamlFactory(JavaPlugin plugin) {
        this.plugin = plugin;
        this.files = new HashMap<>();
    }
}
