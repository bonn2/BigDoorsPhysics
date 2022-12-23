package net.bonn2.bigdoorsphysics;

import org.bukkit.plugin.java.JavaPlugin;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;
        getServer().getPluginManager().registerEvents(new PhysicsListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
