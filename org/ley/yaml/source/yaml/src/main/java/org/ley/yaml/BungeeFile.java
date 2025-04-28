package org.ley.yaml;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeFile implements YamlFile {
    private final Plugin plugin;
    private final String pathToYamlFile;
    private final File rawFile;
    private Configuration yamlFile;

    protected BungeeFile(Plugin plugin, String pathToYamlFolder, String pathToYamlFile) {
        this.plugin = plugin;
        this.pathToYamlFile = pathToYamlFile;
        this.rawFile = new File(pathToYamlFolder, pathToYamlFile);
        this.setup();
    }

    /* Set Manager */
    @Override
    public void set(String path, Object object) {
        if (object instanceof BaseComponent[]) {
            BaseComponent[] components = (BaseComponent[]) object;
            for (int i = 0; i < components.length; i++) {
                BaseComponent component = components[i];

                if (component instanceof TextComponent) {
                    TextComponent text = (TextComponent) component;
                    yamlFile.set(path + ".content." + i + ".text", text.getText());

                    if (text.getHoverEvent() != null) {
                        HoverEvent hover = text.getHoverEvent();
                        yamlFile.set(path + ".content." + i + ".hoverAction", hover.getAction().name());

                        if (hover.getValue() != null && hover.getValue().length > 0) {
                            yamlFile.set(path + ".content." + i + ".hoverValue",
                                    ((TextComponent)hover.getValue()[0]).getText());
                        }
                    }

                    if (text.getClickEvent() != null) {
                        ClickEvent click = text.getClickEvent();
                        yamlFile.set(path + ".content." + i + ".clickAction", click.getAction().name());
                        yamlFile.set(path + ".content." + i + ".clickValue", click.getValue());
                    }
                }
            }
        } else if (object instanceof UUID) {
            yamlFile.set(path, ((UUID) object).toString());
        } else {
            yamlFile.set(path, object);
        }
    }

    /* Get Manager - Single Values */
    @Override
    public Object get(String path) {
        return get(path, null);
    }

    @Override
    public Object get(String path, Object defaultObject) {
        return yamlFile.get(path, defaultObject);
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String defaultValue) {
        return yamlFile.getString(path, defaultValue);
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int defaultValue) {
        return yamlFile.getInt(path, defaultValue);
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        return yamlFile.getDouble(path, defaultValue);
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @Override
    public long getLong(String path, long defaultValue) {
        return yamlFile.getLong(path, defaultValue);
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        return yamlFile.getBoolean(path, defaultValue);
    }

    /* Get Manager - Lists */
    @Override
    public List<String> getStringList(String path) {
        return yamlFile.getStringList(path);
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        List<Integer> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Integer) {
                result.add((Integer) o);
            } else if (o instanceof String) {
                try {
                    result.add(Integer.parseInt((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((int) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).intValue());
            }
        }
        return result;
    }

    @Override
    public List<Double> getDoubleList(String path) {
        List<Double> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Double) {
                result.add((Double) o);
            } else if (o instanceof String) {
                try {
                    result.add(Double.parseDouble((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((double) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).doubleValue());
            }
        }
        return result;
    }

    @Override
    public List<Long> getLongList(String path) {
        List<Long> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Long) {
                result.add((Long) o);
            } else if (o instanceof String) {
                try {
                    result.add(Long.parseLong((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((long) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).longValue());
            }
        }
        return result;
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        List<Boolean> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Boolean) {
                result.add((Boolean) o);
            } else if (o instanceof String) {
                result.add(Boolean.parseBoolean((String) o));
            }
        }
        return result;
    }

    @Override
    public List<Float> getFloatList(String path) {
        List<Float> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Float) {
                result.add((Float) o);
            } else if (o instanceof String) {
                try {
                    result.add(Float.parseFloat((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((float) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).floatValue());
            }
        }
        return result;
    }

    @Override
    public List<Short> getShortList(String path) {
        List<Short> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Short) {
                result.add((Short) o);
            } else if (o instanceof String) {
                try {
                    result.add(Short.parseShort((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((short) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).shortValue());
            }
        }
        return result;
    }

    @Override
    public List<Character> getCharacterList(String path) {
        List<Character> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Character) {
                result.add((Character) o);
            } else if (o instanceof String && ((String) o).length() == 1) {
                result.add(((String) o).charAt(0));
            } else if (o instanceof Number) {
                result.add((char) ((Number) o).intValue());
            }
        }
        return result;
    }

    @Override
    public List<Byte> getByteList(String path) {
        List<Byte> result = new ArrayList<>();
        for (Object o : yamlFile.getList(path, new ArrayList<>())) {
            if (o instanceof Byte) {
                result.add((Byte) o);
            } else if (o instanceof String) {
                try {
                    result.add(Byte.parseByte((String) o));
                } catch (NumberFormatException ignored) {}
            } else if (o instanceof Character) {
                result.add((byte) ((Character) o).charValue());
            } else if (o instanceof Number) {
                result.add(((Number) o).byteValue());
            }
        }
        return result;
    }

    @Override
    public Object getConfigurationSection(String path) {
        return yamlFile.getSection(path);
    }

    /* Special Getters */
    public BaseComponent[] getChatComponent(String path) {
        return getChatComponent(path, null);
    }

    public BaseComponent[] getChatComponent(String path, BaseComponent[] defaultComponent) {
        if (!yamlFile.contains(path + ".content")) return defaultComponent;

        Configuration section = yamlFile.getSection(path + ".content");
        List<BaseComponent> components = new ArrayList<>();

        for (String key : section.getKeys()) {
            String text = yamlFile.getString(path + ".content." + key + ".text");
            if (text == null) continue;

            TextComponent component = new TextComponent(text);

            String hoverAction = yamlFile.getString(path + ".content." + key + ".hoverAction");
            String hoverValue = yamlFile.getString(path + ".content." + key + ".hoverValue");

            if (hoverAction != null && hoverValue != null) {
                try {
                    HoverEvent.Action action = HoverEvent.Action.valueOf(hoverAction);
                    component.setHoverEvent(new HoverEvent(action, new ComponentBuilder(hoverValue).create()));
                } catch (IllegalArgumentException ignored) {}
            }

            String clickAction = yamlFile.getString(path + ".content." + key + ".clickAction");
            String clickValue = yamlFile.getString(path + ".content." + key + ".clickValue");

            if (clickAction != null && clickValue != null) {
                try {
                    ClickEvent.Action action = ClickEvent.Action.valueOf(clickAction);
                    component.setClickEvent(new ClickEvent(action, clickValue));
                } catch (IllegalArgumentException ignored) {}
            }

            components.add(component);
        }

        return components.toArray(new BaseComponent[0]);
    }

    public Configuration getSection(String path) {
        return yamlFile.getSection(path);
    }

    /* File Management */
    @Override
    public void setup() {
        if (!rawFile.exists()) {
            create();
        }
        load();
    }

    @Override
    public void create() {
        try {
            if (!rawFile.getParentFile().exists()) {
                rawFile.getParentFile().mkdirs();
            }

            InputStream in = plugin.getResourceAsStream(pathToYamlFile);
            if (in != null) {
                try {
                    Files.copy(in, rawFile.toPath());
                    return;
                } finally {
                    in.close();
                }
            }

            rawFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        try {
            yamlFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(rawFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            if (yamlFile != null) {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(yamlFile, rawFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(String path) {
        return yamlFile.contains(path);
    }
}