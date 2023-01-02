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
import nl.pim16aap2.bigDoors.BigDoors;
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
        switch (Config.getCollisionMethod()) {
            case BARRIER -> {
                barrierListener = new BarrierListener();
                getServer().getPluginManager().registerEvents(barrierListener, this);
                metrics.addCustomChart(new Metrics.SimplePie("collision_method", () -> "Barrier"));
            }
            case SHULKER -> {
                shulkerListener = new ShulkerListener();
                getServer().getPluginManager().registerEvents(shulkerListener, this);
                metrics.addCustomChart(new Metrics.SimplePie("collision_method", () -> "Shulker"));
            }
            default -> {
                getLogger().severe("Invalid collision method selected! Aborting Loading!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Register Commands
        getLogger().info("Registering Commands");
        getCommand("killbigdoorsphysicsentities").setExecutor(new CommandListener());

        // Register packet modifier
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")
        && Config.cullDistantShulkers()
        && Config.getCollisionMethod().equals(CollisionMethod.SHULKER)) {
            getLogger().info("Enabling ProtocolLib Support");
            ShulkerPacketEditor.register();
        }
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
