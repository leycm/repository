package org.ley.yaml;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.ClickEvent;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YamlFile {
    private final JavaPlugin plugin;
    private final String pathToYamlFile;

    private final File rawFile;
    private FileConfiguration yamlFile;

    public YamlFile(JavaPlugin plugin, String pathToYamlFolder, String pathToYamlFile) {
        this.plugin = plugin;
        this.pathToYamlFile = pathToYamlFile;

        this.rawFile = new File(pathToYamlFolder, pathToYamlFile);

        this.setup();
    }

    /** Set Manager */

    public void set(String path, Object object) {
        switch (object) {
            case Inventory inventory -> {
                if (inventory.getHolder() != null) {
                    InventoryHolder holder = inventory.getHolder();
                    if (holder instanceof Entity) {
                        yamlFile.set(path + ".holder", ((Entity) holder).getUniqueId().toString());
                    } else if (holder instanceof OfflinePlayer) {
                        yamlFile.set(path + ".holder", ((OfflinePlayer) holder).getUniqueId().toString());
                    }

                }

                yamlFile.set(path + ".type", inventory.getSize());

                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && !item.getType().isAir()) {
                        yamlFile.set(path + ".content." + i, item);
                    }
                }
            }
            case Entity entity -> {
                yamlFile.set(path + ".type", entity.getType().name());

                yamlFile.set(path + ".location.world", entity.getWorld().getName());
                yamlFile.set(path + ".location.x", entity.getLocation().getX());
                yamlFile.set(path + ".location.y", entity.getLocation().getY());
                yamlFile.set(path + ".location.z", entity.getLocation().getZ());
                yamlFile.set(path + ".location.yaw", entity.getLocation().getYaw());
                yamlFile.set(path + ".location.pitch", entity.getLocation().getPitch());

                if (entity.getCustomName() != null) {
                    yamlFile.set(path + ".customName", entity.getCustomName());
                    yamlFile.set(path + ".customNameVisible", entity.isCustomNameVisible());
                }

                yamlFile.set(path + ".velocity.x", entity.getVelocity().getX());
                yamlFile.set(path + ".velocity.y", entity.getVelocity().getY());
                yamlFile.set(path + ".velocity.z", entity.getVelocity().getZ());

                yamlFile.set(path + ".glowing", entity.isGlowing());
                yamlFile.set(path + ".silent", entity.isSilent());
                yamlFile.set(path + ".invulnerable", entity.isInvulnerable());

                if (entity instanceof LivingEntity living) {
                    yamlFile.set(path + ".health", living.getHealth());
                    yamlFile.set(path + ".maxHealth", living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    yamlFile.set(path + ".ai", living.hasAI());
                    yamlFile.set(path + ".collidable", living.isCollidable());

                    for (Attribute attribute : Attribute.values()) {
                        yamlFile.set(path + ".attributes." + attribute.toString(), living.getAttribute(attribute));
                    }

                    List<String> effects = new ArrayList<>();
                    for (PotionEffect effect : living.getActivePotionEffects()) {
                        effects.add(effect.getType().getName() + ":" + effect.getAmplifier() + ":" + effect.getDuration());
                    }

                    yamlFile.set(path + ".potionEffects", effects);
                }

                PersistentDataContainer container = ((PersistentDataHolder) entity).getPersistentDataContainer();
                for (NamespacedKey key : container.getKeys()) {
                    Object data = container.get(key, PersistentDataType.STRING); // only try STRING here, or handle all
                    if (data != null) {
                        yamlFile.set(path + ".persistentData." + key.getKey(), data);
                    }
                }
            }
            case BlockData blockData -> {
                yamlFile.set(path + ".blockData", blockData.getAsString());
            }
            case Component[] components -> {
                for (int i = 0; i < components.length; i++) {
                    Component component = components[i];

                    if (component instanceof TextComponent text) {
                        yamlFile.set(path + ".content." + i + ".text", text.content());

                        if (text.hoverEvent() != null) {
                            HoverEvent<?> hover = text.hoverEvent();
                            yamlFile.set(path + ".content." + i + ".hoverAction", hover.action().toString());
                            yamlFile.set(path + ".content." + i + ".hoverValue", ((Component) hover.value()).toString());
                        }

                        if (text.clickEvent() != null) {
                            ClickEvent click = text.clickEvent();
                            yamlFile.set(path + ".content." + i + ".clickAction", click.action().name());
                            yamlFile.set(path + ".content." + i + ".clickValue", click.value());
                        }
                    }
                }
            }
            case UUID uuid -> yamlFile.set(path, uuid.toString());
            case null, default -> yamlFile.set(path, object);
        }
    }

    /** Get Manager */

    public Object get(String path) {return get(path, null);}
    public Object get(String path, Object defaultObject) {return yamlFile.get(path, defaultObject);}

    public String getString(String path) {return getString(path, null);}
    public String getString(String path, String defaultValue) {return yamlFile.getString(path, defaultValue);}

    public int getInt(String path) {return getInt(path, 0);}
    public int getInt(String path, int defaultValue) {return yamlFile.getInt(path, defaultValue);}

    public double getDouble(String path) {return getDouble(path, 0.0);}
    public double getDouble(String path, double defaultValue) {return yamlFile.getDouble(path, defaultValue);}

    public long getLong(String path) {return getLong(path, 0L);}
    public long getLong(String path, long defaultValue) {return yamlFile.getLong(path, defaultValue);}

    public boolean getBoolean(String path) {return getBoolean(path, false);}
    public boolean getBoolean(String path, boolean defaultValue) {return yamlFile.getBoolean(path, defaultValue);}

    public ItemStack getItemStack(String path) {return getItemStack(path, null);}
    public ItemStack getItemStack(String path, ItemStack defaultValue) {return yamlFile.getItemStack(path, defaultValue);}

    public Location getLocation(String path) {return getLocation(path, null);}
    public Location getLocation(String path, Location defaultValue) {return yamlFile.getLocation(path, defaultValue);}

    public Vector getVector(String path) {return getVector(path, null);}
    public Vector getVector(String path, Vector defaultValue) {return yamlFile.getVector(path, defaultValue);}

    public Color getColor(String path) {return getColor(path, null);}
    public Color getColor(String path, Color defaultValue) {return yamlFile.getColor(path, defaultValue);}

    public Entity getEntity(String path) {return getEntity(path, null);}
    public Entity getEntity(String path, Entity defaultEntity) {
        if (!yamlFile.contains(path)) return defaultEntity;

        World world = Bukkit.getWorld(yamlFile.getString(path + ".location.world"));
        if (world == null) return defaultEntity;

        double x = yamlFile.getDouble(path + ".location.x");
        double y = yamlFile.getDouble(path + ".location.y");
        double z = yamlFile.getDouble(path + ".location.z");
        float yaw = (float) yamlFile.getDouble(path + ".location.yaw");
        float pitch = (float) yamlFile.getDouble(path + ".location.pitch");

        Location loc = new Location(world, x, y, z, yaw, pitch);

        EntityType type = EntityType.valueOf(yamlFile.getString(path + ".type", "ZOMBIE"));
        Entity newEntity = world.spawnEntity(loc, type, CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
            if (yamlFile.contains(path + ".customName")) {
                entity.setCustomName(yamlFile.getString(path + ".customName"));
                entity.setCustomNameVisible(yamlFile.getBoolean(path + ".customNameVisible", false));
            }

            double vx = yamlFile.getDouble(path + ".velocity.x", 0);
            double vy = yamlFile.getDouble(path + ".velocity.y", 0);
            double vz = yamlFile.getDouble(path + ".velocity.z", 0);

            entity.setVelocity(new Vector(vx, vy, vz));

            entity.setGlowing(yamlFile.getBoolean(path + ".glowing", false));
            entity.setSilent(yamlFile.getBoolean(path + ".silent", false));
            entity.setInvulnerable(yamlFile.getBoolean(path + ".invulnerable", false));

            if (entity instanceof LivingEntity living) {
                living.setHealth(yamlFile.getDouble(path + ".health", 20));
                if (yamlFile.contains(path + ".ai")) living.setAI(yamlFile.getBoolean(path + ".ai"));
                if (yamlFile.contains(path + ".collidable")) living.setCollidable(yamlFile.getBoolean(path + ".collidable"));

                if (yamlFile.contains(path + ".attributes")) {
                    ConfigurationSection attrSec = yamlFile.getConfigurationSection(path + ".attributes");
                    for (String key : attrSec.getKeys(false)) {
                        try {
                            Attribute attribute = Attribute.valueOf(key);
                            double value = yamlFile.getDouble(path + ".attributes." + key);
                            if (living.getAttribute(attribute) != null) {
                                living.getAttribute(attribute).setBaseValue(value);
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                }

                if (yamlFile.contains(path + ".potionEffects")) {
                    List<String> list = yamlFile.getStringList(path + ".potionEffects");
                    for (String str : list) {
                        String[] parts = str.split(":");
                        if (parts.length >= 3) {
                            try {
                                PotionEffectType typePE = PotionEffectType.getByName(parts[0]);
                                int amplifier = Integer.parseInt(parts[1]);
                                int duration = Integer.parseInt(parts[2]);
                                if (typePE != null) {
                                    living.addPotionEffect(new PotionEffect(typePE, duration, amplifier));
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            if (yamlFile.contains(path + ".persistentData")) {
                PersistentDataContainer container = entity.getPersistentDataContainer();
                ConfigurationSection sec = yamlFile.getConfigurationSection(path + ".persistentData");

                for (String key : sec.getKeys(false)) {
                    NamespacedKey namespacedKey = new NamespacedKey("custom", key);
                    container.set(namespacedKey, PersistentDataType.STRING, sec.getString(key));
                }
            }
        });

        return newEntity;
    }

    public Entity getEntity(String path, LivingEntity entity) {return getEntity(path, entity,  null);}
    public Entity getEntity(String path, LivingEntity entity, Entity defaultEntity) {
        if (!yamlFile.contains(path)) return defaultEntity;

        World world = Bukkit.getWorld(yamlFile.getString(path + ".location.world"));
        if (world == null) return defaultEntity;

        double x = yamlFile.getDouble(path + ".location.x");
        double y = yamlFile.getDouble(path + ".location.y");
        double z = yamlFile.getDouble(path + ".location.z");
        float yaw = (float) yamlFile.getDouble(path + ".location.yaw");
        float pitch = (float) yamlFile.getDouble(path + ".location.pitch");

        Location loc = new Location(world, x, y, z, yaw, pitch);
        entity.teleport(loc);

        if (yamlFile.contains(path + ".customName")) {
            entity.setCustomName(yamlFile.getString(path + ".customName"));
            entity.setCustomNameVisible(yamlFile.getBoolean(path + ".customNameVisible", false));
        }

        double vx = yamlFile.getDouble(path + ".velocity.x", 0);
        double vy = yamlFile.getDouble(path + ".velocity.y", 0);
        double vz = yamlFile.getDouble(path + ".velocity.z", 0);
        entity.setVelocity(new Vector(vx, vy, vz));

        entity.setGlowing(yamlFile.getBoolean(path + ".glowing", false));
        entity.setSilent(yamlFile.getBoolean(path + ".silent", false));
        entity.setInvulnerable(yamlFile.getBoolean(path + ".invulnerable", false));

        if (entity instanceof LivingEntity living) {
            living.setHealth(yamlFile.getDouble(path + ".health", 20));
            if (yamlFile.contains(path + ".ai")) living.setAI(yamlFile.getBoolean(path + ".ai"));
            if (yamlFile.contains(path + ".collidable")) living.setCollidable(yamlFile.getBoolean(path + ".collidable"));

            if (yamlFile.contains(path + ".attributes")) {
                ConfigurationSection attrSec = yamlFile.getConfigurationSection(path + ".attributes");
                for (String key : attrSec.getKeys(false)) {
                    try {
                        Attribute attribute = Attribute.valueOf(key);
                        double value = yamlFile.getDouble(path + ".attributes." + key);
                        if (living.getAttribute(attribute) != null) {
                            living.getAttribute(attribute).setBaseValue(value);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            if (yamlFile.contains(path + ".potionEffects")) {
                List<String> list = yamlFile.getStringList(path + ".potionEffects");
                for (String str : list) {
                    String[] parts = str.split(":");
                    if (parts.length >= 3) {
                        try {
                            PotionEffectType typePE = PotionEffectType.getByName(parts[0]);
                            int amplifier = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]);
                            if (typePE != null) {
                                living.addPotionEffect(new PotionEffect(typePE, duration, amplifier));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        if (yamlFile.contains(path + ".persistentData")) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            ConfigurationSection sec = yamlFile.getConfigurationSection(path + ".persistentData");

            for (String key : sec.getKeys(false)) {
                NamespacedKey namespacedKey = new NamespacedKey("custom", key);
                container.set(namespacedKey, PersistentDataType.STRING, sec.getString(key));
            }
        }

        return entity;
    }


    public BlockData getBlockData(String path) {return getBlockData(path, null);}
    public BlockData getBlockData(String path, BlockData defaultBlockData) {
        return Material.matchMaterial(yamlFile.getString(path).split("\\[")[0]).createBlockData(yamlFile.getString(path));
    }

    public Inventory getInventory(String path, String title) {return getInventory(path, title, null);}
    public Inventory getInventory(String path, String title, Inventory defaultInventory) {
        if (!yamlFile.contains(path)) return defaultInventory;

        int size = yamlFile.getInt(path + ".type", 27);
        UUID uuid = UUID.fromString(yamlFile.getString(path + ".holder", UUID.randomUUID().toString()));
        InventoryHolder holder = Bukkit.getPlayer(uuid);


        Inventory inventory = Bukkit.createInventory(holder, size, title);

        if (yamlFile.contains(path + ".content")) {
            ConfigurationSection section = yamlFile.getConfigurationSection(path + ".content");
            for (String key : section.getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = yamlFile.getItemStack(path + ".content." + slot);
                if (item != null) {
                    inventory.setItem(slot, item);
                }
            }
        }

        return inventory;
    }

    public Component[] getChatComponent(String path) {return getChatComponent(path, null);}
    public Component[] getChatComponent(String path, Component[] defaultComponent) {
        if (!yamlFile.contains(path + ".content")) return defaultComponent;

        ConfigurationSection section = yamlFile.getConfigurationSection(path + ".content");
        List<Component> components = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            String text = yamlFile.getString(path + ".content." + key + ".text");
            if (text == null) continue;

            TextComponent.Builder builder = Component.text().content(text);

            String hoverAction = yamlFile.getString(path + ".content." + key + ".hoverAction");
            String hoverValue = yamlFile.getString(path + ".content." + key + ".hoverValue");

            if (hoverAction != null && hoverValue != null) {
                builder.hoverEvent(HoverEvent.showText(Component.text(hoverValue)));
            }

            String clickAction = yamlFile.getString(path + ".content." + key + ".clickAction");
            String clickValue = yamlFile.getString(path + ".content." + key + ".clickValue");

            if (clickAction != null && clickValue != null) {
                builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickAction), clickValue));
            }

            components.add(builder.build());
        }

        return components.toArray(new Component[0]);
    }

    public List<String> getStringList(String path) {return yamlFile.getStringList(path);}

    public List<Integer> getIntegerList(String path) {return yamlFile.getIntegerList(path);}

    public List<Double> getDoubleList(String path) {return yamlFile.getDoubleList(path);}

    public List<Long> getLongList(String path) {return yamlFile.getLongList(path);}

    public List<Boolean> getBooleanList(String path) {return yamlFile.getBooleanList(path);}

    public List<Float> getFloatList(String path) {return yamlFile.getFloatList(path);}

    public List<Short> getShortList(String path) {return yamlFile.getShortList(path);}

    public List<Character> getCharacterList(String path) {return yamlFile.getCharacterList(path);}

    public List<Byte> getByteList(String path) {return yamlFile.getByteList(path);}

    public ConfigurationSection getConfigurationSection(String path) {return yamlFile.getConfigurationSection(path);}

    /** File Manager  */

    public void setup() {
        if (!rawFile.exists()) {create();}load();
    }

    public void create() {
        try {
            if (!rawFile.getParentFile().exists()) {
                rawFile.getParentFile().mkdirs();
            }

            InputStream in = plugin.getResource(pathToYamlFile);
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

    public void load() {
        try {
            yamlFile = YamlConfiguration.loadConfiguration(rawFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (yamlFile != null) {yamlFile.save(rawFile);}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

