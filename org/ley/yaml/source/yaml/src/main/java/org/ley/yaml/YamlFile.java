package org.ley.yaml;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
        if (object instanceof Inventory) {
            Inventory inventory = (Inventory) object;

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

        } else if (object instanceof Entity entity) {
            String base = path;

            yamlFile.set(base + ".type", entity.getType().name());

            yamlFile.set(base + ".location.world", entity.getWorld().getName());
            yamlFile.set(base + ".location.x", entity.getLocation().getX());
            yamlFile.set(base + ".location.y", entity.getLocation().getY());
            yamlFile.set(base + ".location.z", entity.getLocation().getZ());
            yamlFile.set(base + ".location.yaw", entity.getLocation().getYaw());
            yamlFile.set(base + ".location.pitch", entity.getLocation().getPitch());

            if (entity.getCustomName() != null) {
                yamlFile.set(base + ".customName", entity.getCustomName());
                yamlFile.set(base + ".customNameVisible", entity.isCustomNameVisible());
            }

            yamlFile.set(base + ".velocity.x", entity.getVelocity().getX());
            yamlFile.set(base + ".velocity.y", entity.getVelocity().getY());
            yamlFile.set(base + ".velocity.z", entity.getVelocity().getZ());

            yamlFile.set(base + ".glowing", entity.isGlowing());
            yamlFile.set(base + ".silent", entity.isSilent());
            yamlFile.set(base + ".invulnerable", entity.isInvulnerable());

            if (entity instanceof LivingEntity living) {
                yamlFile.set(base + ".health", living.getHealth());
                yamlFile.set(base + ".maxHealth", living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                yamlFile.set(base + ".ai", living.hasAI());
                yamlFile.set(base + ".collidable", living.isCollidable());

                for(Attribute attribute : Attribute.values()) {
                    yamlFile.set(base + ".attributes." + attribute.toString(), living.getAttribute(attribute));
                }

                List<String> effects = new ArrayList<>();
                for (PotionEffect effect : living.getActivePotionEffects()) {
                    effects.add(effect.getType().getName() + ":" + effect.getAmplifier() + ":" + effect.getDuration());
                }

                yamlFile.set(base + ".potionEffects", effects);
            }

            PersistentDataContainer container = ((PersistentDataHolder) entity).getPersistentDataContainer();
            for (NamespacedKey key : container.getKeys()) {
                Object data = container.get(key, PersistentDataType.STRING); // only try STRING here, or handle all
                if (data != null) {
                    yamlFile.set(base + ".persistentData." + key.getKey(), data);
                }
            }

        } else if (object instanceof UUID) {
            yamlFile.set(path, object.toString());
        } else {
            yamlFile.set(path, object);
        }
    }

    /** Get Manager */

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

    public Inventory getInventory(String path, String title) {return getInventory(path, title, null);}
    public Inventory getInventory(String path, String title, Inventory def) {
        if (!yamlFile.contains(path)) return def;

        int size = yamlFile.getInt(path + ".type", 27);

        InventoryHolder holder = null;

        if (yamlFile.contains(path + ".holder")) {
            try {
                UUID uuid = UUID.fromString(yamlFile.getString(path + ".holder"));
                holder = Bukkit.getPlayer(uuid);

            } catch (IllegalArgumentException ignored) {}
        }

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




    /** File Manager  */
    public void setup() {
        if (!rawFile.exists()) {create();}load();
    }

    public void create() {
        try {
            if (!rawFile.getParentFile().exists()) {
                rawFile.getParentFile().mkdirs();
            }
            // Only use the file name for getting resource from inside the plugin
            InputStream in = plugin.getResource(pathToYamlFile);
            if (in != null) {
                Files.copy(in, rawFile.toPath());
            } else {
                rawFile.createNewFile();
            }

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

