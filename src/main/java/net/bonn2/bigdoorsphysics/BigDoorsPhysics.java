package net.bonn2.bigdoorsphysics;

import net.bonn2.bigdoorsphysics.barriermethod.BarrierListener;
import net.bonn2.bigdoorsphysics.barriermethod.ColliderBlock;
import net.bonn2.bigdoorsphysics.bstats.Metrics;
import net.bonn2.bigdoorsphysics.shulkermethod.ColliderShulker;
import net.bonn2.bigdoorsphysics.shulkermethod.CommandListener;
import net.bonn2.bigdoorsphysics.shulkermethod.ShulkerListener;
import net.bonn2.bigdoorsphysics.shulkermethod.ShulkerPacketEditor;
import net.bonn2.bigdoorsphysics.util.CollisionMethod;
import net.bonn2.bigdoorsphysics.util.Config;
import net.bonn2.bigdoorsphysics.util.ModrinthUpdateChecker;
import net.bonn2.bigdoorsphysics.versions.VersionUtil;
import net.bonn2.bigdoorsphysics.versions.VersionUtilProvider;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.util.DoorType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;
    public static VersionUtil VERSION_UTIL;
    public static BarrierListener BARRIER_LISTENER;
    public static ShulkerListener SHULKER_LISTENER;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;

        // Load the config
        getLogger().info("Loading Config");
        Config.load();

        // Get compatible VersionUtil
        VERSION_UTIL = VersionUtilProvider.getVersionUtil();
        if (VERSION_UTIL == null) {
            getLogger().severe("Failed to load VersionUtil! This likely means the plugin needs an update to work on this minecraft version");
            getLogger().severe("Please check for an update, and if none are available report this to bonn2.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Enable metrics
        Metrics metrics = new Metrics(this, 17236);

        // Register Events
        getLogger().info("Registering Events");
        if (Config.getCollisionMethod().containsValue(CollisionMethod.BARRIER)) {
            BARRIER_LISTENER = new BarrierListener();
            getServer().getPluginManager().registerEvents(BARRIER_LISTENER, this);

            // Start collisions updater
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> BARRIER_LISTENER.updateCollisions(), 1, 1);
        }
        if (Config.getCollisionMethod().containsValue(CollisionMethod.SHULKER)) {
            SHULKER_LISTENER = new ShulkerListener();
            getServer().getPluginManager().registerEvents(SHULKER_LISTENER, this);

            // Start collisions updater
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> SHULKER_LISTENER.updateCollisions(), 1, 1);
        }

        metrics.addCustomChart(new Metrics.SimplePie("door_method", () -> Config.getCollisionMethod().getOrDefault(DoorType.DOOR, CollisionMethod.NONE).name()));
        metrics.addCustomChart(new Metrics.SimplePie("drawbridge_method", () -> Config.getCollisionMethod().getOrDefault(DoorType.DRAWBRIDGE, CollisionMethod.NONE).name()));
        metrics.addCustomChart(new Metrics.SimplePie("portcullis_method", () -> Config.getCollisionMethod().getOrDefault(DoorType.PORTCULLIS, CollisionMethod.NONE).name()));
        metrics.addCustomChart(new Metrics.SimplePie("sliding_method", () -> Config.getCollisionMethod().getOrDefault(DoorType.SLIDINGDOOR, CollisionMethod.NONE).name()));

        // Register Commands
        getLogger().info("Registering Commands");
        getCommand("killbigdoorsphysicsentities").setExecutor(new CommandListener());

        // Register packet modifier
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().info("Enabling ProtocolLib Support");
            ShulkerPacketEditor.register();
        }

        // Spigot warning
        if (VersionUtil.isSpigot() && VersionUtil.getMajorVersion() >= 19) {
            getLogger().warning("You are running Spigot on >= 1.19. Because of this the plugin will not function as smoothly as it could.");
            getLogger().warning("It is highly recommended to use Paper or a Paper-Fork");
        }

        // Check for updates
        if (Config.checkForUpdates())
            ModrinthUpdateChecker.check(this);
    }

    @Override
    public void onDisable() {
        // Remove old barrier blocks
        getLogger().info("Removing leftover colliders");
        if (BARRIER_LISTENER != null) {
            for (Long id : BARRIER_LISTENER.getColliders().keySet()) {
                for (ColliderBlock colliderBlock : BARRIER_LISTENER.getColliders().get(id)) {
                    colliderBlock.remove();
                }
            }
        }
        if (SHULKER_LISTENER != null) {
            for (Long id : SHULKER_LISTENER.getColliders().keySet()) {
                for (ColliderShulker colliderShulker : SHULKER_LISTENER.getColliders().get(id)) {
                    colliderShulker.remove();
                }
            }
        }
    }
}
