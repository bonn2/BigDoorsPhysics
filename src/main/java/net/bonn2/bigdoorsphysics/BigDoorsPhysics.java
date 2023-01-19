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
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.util.DoorType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;

    private BarrierListener barrierListener;
    private ShulkerListener shulkerListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;

        // Check if installed BigDoors version supports BlockMover#getSavedBlocks()
        if (BigDoors.get().getBuild() < 1119) {
            getLogger().severe("BigDoors version is too old! Please update before using!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading Config");
        Config.load();

        // Enable metrics
        Metrics metrics = new Metrics(this, 17236);

        // Register Events
        getLogger().info("Registering Events");
        if (Config.getCollisionMethod().containsValue(CollisionMethod.BARRIER)) {
            barrierListener = new BarrierListener();
            getServer().getPluginManager().registerEvents(barrierListener, this);
        }
        if (Config.getCollisionMethod().containsValue(CollisionMethod.SHULKER)) {
            shulkerListener = new ShulkerListener();
            getServer().getPluginManager().registerEvents(shulkerListener, this);
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

        // Check for updates
        if (Config.checkForUpdates())
            ModrinthUpdateChecker.check(this);
    }

    @Override
    public void onDisable() {
        // Remove old barrier blocks
        getLogger().info("Removing leftover colliders");
        if (barrierListener != null) {
            for (Long id : barrierListener.getColliders().keySet()) {
                for (ColliderBlock colliderBlock : barrierListener.getColliders().get(id)) {
                    colliderBlock.remove();
                }
            }
        }
        if (shulkerListener != null) {
            for (Long id : shulkerListener.getColliders().keySet()) {
                for (ColliderShulker colliderShulker : shulkerListener.getColliders().get(id)) {
                    colliderShulker.remove();
                }
            }
        }
    }
}
