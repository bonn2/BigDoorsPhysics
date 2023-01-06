package net.bonn2.bigdoorsphysics.util;

import nl.pim16aap2.bigDoors.util.DoorType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
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
    static boolean hideBarriers = true;
    static boolean movePlayerWithBarrier = true;

    static String collisionMethodComment =
            """
            # How the plugin should create the colliders
            # Valid options: BARRIER, SHULKER, NONE
            # Any invalid selection will be set to the default for your version
            """;

    static String movePlayerWithShulkerComment =
            """
            ### Shulker Options
            
            # Make the player move with the shulker, this only has an effect when the shulker and player clip into one another.
            # It will not have an effect when the shulker and player are not clipped into each other
            # Potential tps impact on larger player and door counts
            """;

    static String moveEntityWithShulkerComment =
            """
            # Make all non-player entities move with the shulker, this only has an effect when the shulker and entity clip into one another.
            # It will not have an effect when the shulker and entity are not clipped into each other
            # Larger performance impact than with players, due to the usually larger amount of entities on a server
            """;

    static String correctEndingClippingComment =
            """
            # Verify that the moved entity is not clipping into the door when it finishes moving
            # If a entity is standing on top of a door when it finishes moving they will be
            # teleported up a small amount to prevent them from falling through the door
            # This will only apply to entities / players if they are enabled above
            # This should have a minimal performance impact
            """;

    static String cullDistantShulkersComment =
            """
            # Hide shulkers that are far away from the player
            # NOTE: This requires ProtocolLib to work
            """;

    static String shulkerCullDistanceComment =
            """
            # How many blocks away a shulker needs to be to be culled
            # Set to number less than 0 to always cull shulkers
            # NOTE: If player interacts will culled shulker it will behave similarly to a ghost block
            """;

    static String hideBarriersComment =
            """
            ### Barrier Options

            # Use extra packets to hide barriers when they are far away
            # This may have a medium performance impact, primarily fps
            """;

    static String movePlayerWithBarrierComment =
            """
            # Move players who are near the door in accordance with the direction of the door
            # This may have a small performance impact, primarily tps
            """;

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
                    case "BARRIER" -> {
                        collisionMethod.put(DoorType.DOOR, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.BARRIER);
                        collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.BARRIER);
                        needToUpgrade = true;
                    }
                    case "SHULKER" -> {
                        collisionMethod.put(DoorType.DOOR, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.DRAWBRIDGE, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.PORTCULLIS, CollisionMethod.SHULKER);
                        collisionMethod.put(DoorType.SLIDINGDOOR, CollisionMethod.SHULKER);
                        needToUpgrade = true;
                    }
                    default -> {
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
                    }
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
                            hideBarriersComment +
                            "hide-barriers: " +
                            hideBarriers +
                            "\n\n" +
                            movePlayerWithBarrierComment +
                            "move-player-with-barrier: " +
                            movePlayerWithBarrier +
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

    public static boolean hideBarriers() {
        return hideBarriers;
    }

    public static boolean movePlayerWithBarrier() {
        return movePlayerWithBarrier;
    }
}
