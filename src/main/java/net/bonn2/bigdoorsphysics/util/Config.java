package net.bonn2.bigdoorsphysics.util;

import nl.pim16aap2.bigDoors.util.DoorType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class Config {

    static final File configFile = new File(PLUGIN.getDataFolder().getAbsolutePath() + File.separator + "config.yml");

    static Map<DoorType, CollisionMethod> collisionMethod = new HashMap<>(4);
    static boolean movePlayerWithShulker = true;
    static boolean moveEntityWithShulker = true;
    static boolean correctEndingClipping = true;
    static boolean cullDistantShulkers = true;
    static int shulkerCullDistance = 4;
    static boolean spawnShulkersOnDoor = PLUGIN.getServer().getPluginManager().isPluginEnabled("ProtocolLib");
    static boolean removeShulkersOnChunkLoad = false;
    static boolean hideBarriers = true;
    static boolean movePlayerWithBarrier = true;
    static boolean checkForUpdates = true;
    static boolean detailedLogging = false;

    static String collisionMethodComment =
            "# How the plugin should create the colliders\n" +
            "# Valid options: BARRIER, SHULKER, NONE\n" +
            "# Any invalid selection will be set to the default for your version\n";

    static String movePlayerWithShulkerComment =
            "### Shulker Options\n" +
            "\n" +
            "# Make the player move with the shulker, this only has an effect when the shulker and player clip into one another.\n" +
            "# It will not have an effect when the shulker and player are not clipped into each other\n" +
            "# Potential tps impact on larger player and door counts\n";

    static String moveEntityWithShulkerComment =
            "# Make all non-player entities move with the shulker, this only has an effect when the shulker and entity clip into one another.\n" +
            "# It will not have an effect when the shulker and entity are not clipped into each other\n" +
            "# Larger performance impact than with players, due to the usually larger amount of entities on a server\n";

    static String correctEndingClippingComment =
            "# Verify that the moved entity is not clipping into the door when it finishes moving\n" +
            "# If a entity is standing on top of a door when it finishes moving they will be\n" +
            "# teleported up a small amount to prevent them from falling through the door\n" +
            "# This will only apply to entities / players if they are enabled above\n" +
            "# This should have a minimal performance impact\n";

    static String cullDistantShulkersComment =
            "# Hide shulkers that are far away from the player\n" +
            "# NOTE: This requires ProtocolLib to work\n";

    static String shulkerCullDistanceComment =
            "# How many blocks away a shulker needs to be to be culled\n" +
            "# Set to number less than 0 to always cull shulkers\n" +
            "# NOTE: If player interacts will culled shulker it will behave similarly to a ghost block\n";

    static String spawnShulkersOnDoorComment =
            "# If shulkers should be spawned on the door, or far away\n" +
            "# When this is set to true the shulkers will be spawned near their final locations\n" +
            "# When this is set to false the shulkers will be spawned ~100k blocks above the world height to hide them while they are being set up\n" +
            "# Enabling this can help with compatibility with region plugins (Specifically if you are using regions to control mob spawning)\n" +
            "# By default this is enabled if ProtocolLib is on the server and shulkers will instead be hidden during setup using packets\n";

    static String removeShulkersOnChunkLoadComment =
            "# This setting will check for leftover shulkers whenever a chunk loads\n" +
            "# ONLY enable this setting if you have to run /killbigdoorsphysicsentities frequently\n" +
            "# This setting may have a performance impact if your server has a lot of entities\n";

    static String hideBarriersComment =
            "### Barrier Options\n" +
            "\n" +
            "# Use extra packets to hide barriers when they are far away\n" +
            "# This may have a medium performance impact, primarily fps\n";

    static String movePlayerWithBarrierComment =
            "# Move players who are near the door in accordance with the direction of the door\n" +
            "# This may have a small performance impact, primarily tps\n";

    static String checkForUpdatesComment =
            "### Other Options\n" +
            "\n" +
            "# Whether or not to check for updates via https://modrinth.com/\n" +
            "# Updates will only be checked for on startup\n";

    static String detailedLoggingComment =
            "# Enable more detailed logging for debugging\n";

    public static void load() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            if (PLUGIN.getDataFolder().mkdirs()) PLUGIN.getLogger().info("Created data folder");
            if (!configFile.createNewFile()) {
                try (FileInputStream input = new FileInputStream(configFile)) {
                    config.loadFromString(new String(input.readAllBytes()));
                } catch (InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }
            } else {
                write();
                try (FileInputStream input = new FileInputStream(configFile)) {
                    config.loadFromString(new String(input.readAllBytes()));
                } catch (InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Whether the config needs to be upgraded, if set to true the config will be rewritten to disk
        boolean needToUpgrade = false;

        // Get CollisionMethod
        if (config.contains("method")) {
            String methodConfig = config.getString("method");
            if (methodConfig == null) {
                collisionMethod.put(DoorType.DOOR, CollisionMethod.safeValueOf(config.getString("method.door", CollisionMethod.getDefaultValue().name())));
                collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.safeValueOf(config.getString("method.drawbridge", CollisionMethod.getDefaultValue().name())));
                collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.safeValueOf(config.getString("method.portcullis", CollisionMethod.getDefaultValue().name())));
                collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.safeValueOf(config.getString("method.sliding", CollisionMethod.getDefaultValue().name())));
            }
            else {
                switch (methodConfig.toUpperCase()) {
                    case "BARRIER":
                        collisionMethod.put(DoorType.DOOR, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.BARRIER);
                        needToUpgrade = true;
                        break;
                    case "SHULKER":
                        collisionMethod.put(DoorType.DOOR, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.SHULKER);
                        needToUpgrade = true;
                        break;
                    default:
                        collisionMethod.put(DoorType.DOOR, CollisionMethod.safeValueOf(config.getString("method.door", CollisionMethod.getDefaultValue().name())));
                        collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.safeValueOf(config.getString("method.drawbridge", CollisionMethod.getDefaultValue().name())));
                        collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.safeValueOf(config.getString("method.portcullis", CollisionMethod.getDefaultValue().name())));
                        collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.safeValueOf(config.getString("method.sliding", CollisionMethod.getDefaultValue().name())));
                        if (!(config.contains("method.door")
                                && config.contains("method.drawbridge")
                                && config.contains("method.portcullis")
                                && config.contains("method.sliding"))) {
                            needToUpgrade = true;
                        }
                        break;
                }
            }
        } else {
            warnMissing("method");
            collisionMethod.put(DoorType.DOOR, CollisionMethod.getDefaultValue());
            collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.getDefaultValue());
            collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.getDefaultValue());
            collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.getDefaultValue());
            needToUpgrade = true;
        }

        // Get movePlayerWithShulker
        if (config.contains("move-player-with-shulker"))
            movePlayerWithShulker = config.getBoolean("move-player-with-shulker");
        else {
            warnMissing("move-player-with-shulker");
            needToUpgrade = true;
        }

        // Get moveEntityWithShulker
        if (config.contains("move-entity-with-shulker"))
            moveEntityWithShulker = config.getBoolean("move-entity-with-shulker");
        else {
            warnMissing("move-entity-with-shulker");
            needToUpgrade = true;
        }

        // Get correctEndingClipping
        if (config.contains("correct-ending-clipping"))
            correctEndingClipping = config.getBoolean("correct-ending-clipping");
        else {
            warnMissing("correct-ending-clipping");
            needToUpgrade = true;
        }

        // Get cullDistantShulkers
        if (config.contains("cull-distant-shulkers"))
            cullDistantShulkers = config.getBoolean("cull-distant-shulkers");
        else {
            warnMissing("cull-distant-shulkers");
            needToUpgrade = true;
        }

        // Get shulkerCullDistance
        if (config.contains("shulker-cull-distance"))
            shulkerCullDistance = config.getInt("shulker-cull-distance");
        else {
            warnMissing("shulker-cull-distance");
            needToUpgrade = true;
        }

        // Get spawnShulkersOnDoor
        if (config.contains("spawn-shulkers-on-door"))
            spawnShulkersOnDoor = config.getBoolean("spawn-shulkers-on-door");
        else {
            warnMissing("spawn-shulkers-on-door");
            needToUpgrade = true;
        }

        // Get removeShulkersOnChunkLoad
        if (config.contains("remove-shulkers-on-chunk-load"))
            removeShulkersOnChunkLoad = config.getBoolean("remove-shulkers-on-chunk-load");
        else {
            warnMissing("remove-shulkers-on-chunk-load");
            needToUpgrade = true;
        }

        // Get hideBarriers
        if (config.contains("hide-barriers"))
            hideBarriers = config.getBoolean("hide-barriers");
        else {
            warnMissing("hide-barriers");
            needToUpgrade = true;
        }

        // Get movePlayerWithBarrier
        if (config.contains("move-players")) {
            warnUpgrade("move-players", "move-player-with-barrier");
            movePlayerWithBarrier = config.getBoolean("move-players");
            needToUpgrade = true;
        } else if (config.contains("move-player-with-barrier")) {
            movePlayerWithBarrier = config.getBoolean("move-player-with-barrier");
        }
        else {
            warnMissing("move-player-with-barrier");
            needToUpgrade = true;
        }

        // Get checkForUpdates
        if (config.contains("check-for-updates")) {
            checkForUpdates = config.getBoolean("check-for-updates");
        } else {
            warnMissing("check-for-updates");
            needToUpgrade = true;
        }

        // Get detailedLogging
        if (config.contains("detailed-logging")) {
            detailedLogging = config.getBoolean("detailed-logging");
        } else {
            warnMissing("detailed-logging");
            needToUpgrade = true;
        }

        // Write updated config if necessary
        if (needToUpgrade) write();
    }

    /**
     * Print a warning to console about upgrading config keys
     * @param oldKey The original key that the config was found to be using
     * @param newKey The new key that config uses now
     */
    private static void warnUpgrade(String oldKey, String newKey) {
        PLUGIN.getLogger().warning("Found legacy key: " + oldKey);
        PLUGIN.getLogger().warning("It will be upgraded to: " + newKey);
    }

    private static void warnMissing(String key) {
        PLUGIN.getLogger().warning("Failed to find key: " + key);
        PLUGIN.getLogger().warning("The config will be refreshed with the default value for this key");
    }

    private static void write() {
        try (FileOutputStream output = new FileOutputStream(configFile, false)) {
            String builder =
                    ""+
                            collisionMethodComment +
                            "method:" +
                            "\n  door: " +
                            collisionMethod.getOrDefault(DoorType.DOOR, CollisionMethod.getDefaultValue()) +
                            "\n  drawbridge: " +
                            collisionMethod.getOrDefault(DoorType.DRAWBRIDGE, CollisionMethod.getDefaultValue()) +
                            "\n  portcullis: " +
                            collisionMethod.getOrDefault(DoorType.PORTCULLIS, CollisionMethod.getDefaultValue()) +
                            "\n  sliding: " +
                            collisionMethod.getOrDefault(DoorType.SLIDINGDOOR, CollisionMethod.getDefaultValue()) +
                            "\n\n" +
                            movePlayerWithShulkerComment +
                            "move-player-with-shulker: " +
                            moveEntityWithShulker +
                            "\n\n" +
                            moveEntityWithShulkerComment +
                            "move-entity-with-shulker: " +
                            moveEntityWithShulker +
                            "\n\n" +
                            correctEndingClippingComment +
                            "correct-ending-clipping: " +
                            correctEndingClipping +
                            "\n\n" +
                            cullDistantShulkersComment +
                            "cull-distant-shulkers: " +
                            cullDistantShulkers +
                            "\n\n" +
                            shulkerCullDistanceComment +
                            "shulker-cull-distance: " +
                            shulkerCullDistance +
                            "\n\n" +
                            spawnShulkersOnDoorComment +
                            "spawn-shulkers-on-door: " +
                            spawnShulkersOnDoor +
                            "\n\n" +
                            removeShulkersOnChunkLoadComment +
                            "remove-shulkers-on-chunk-load: " +
                            removeShulkersOnChunkLoad +
                            "\n\n" +
                            hideBarriersComment +
                            "hide-barriers: " +
                            hideBarriers +
                            "\n\n" +
                            movePlayerWithBarrierComment +
                            "move-player-with-barrier: " +
                            movePlayerWithBarrier +
                            "\n\n" +
                            checkForUpdatesComment +
                            "check-for-updates: " +
                            checkForUpdates +
                            "\n\n" +
                            detailedLoggingComment +
                            "detailed-logging: " +
                            detailedLogging +
                            "\n";
            output.write(builder.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<DoorType, CollisionMethod> getCollisionMethod() {
        return collisionMethod;
    }

    public static boolean movePlayerWithShulker() {
        return movePlayerWithShulker;
    }

    public static boolean moveEntityWithShulker() {
        return moveEntityWithShulker;
    }

    public static boolean correctEndingClipping() {
        return correctEndingClipping;
    }

    public static boolean cullDistantShulkers() {
        return cullDistantShulkers;
    }

    public static int shulkerCullDistance() {
        return shulkerCullDistance;
    }

    public static boolean spawnShulkersOnDoor() {
        return spawnShulkersOnDoor;
    }

    public static boolean removeShulkersOnChunkLoad() {
        return removeShulkersOnChunkLoad;
    }

    public static boolean hideBarriers() {
        return hideBarriers;
    }

    public static boolean movePlayerWithBarrier() {
        return movePlayerWithBarrier;
    }

    public static boolean checkForUpdates() {
        return checkForUpdates;
    }

    public static boolean detailedLogging() {
        return detailedLogging;
    }
}
