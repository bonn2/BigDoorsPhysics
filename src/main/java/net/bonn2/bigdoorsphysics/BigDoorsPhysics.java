package net.bonn2.bigdoorsphysics;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;
    public static YamlConfiguration CONFIG = new YamlConfiguration();

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;

        // Load config
        File configFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml");
        try {
            if (getDataFolder().mkdirs()) getLogger().info("Created data folder");
            if (configFile.createNewFile()) {
                try (FileOutputStream output = new FileOutputStream(configFile)) {
                    output.write(
                                    """
                                    ### BigDoorsPhysics Config
                                    
                                    # Use extra packets to hide barriers when they are far away
                                    # This may have a medium performance impact, primarily fps
                                    hide-barriers: true
                                    
                                    # Move players who are near the door in accordance with the direction of the door
                                    # This may have a small performance impact, primarily tps
                                    move-players: true
                                    
                                    # DO NOT TOUCH
                                    config-version: 1
                                    """.getBytes(StandardCharsets.UTF_8)
                    );
                }
            }
            try (FileInputStream input = new FileInputStream(configFile)) {
                CONFIG.loadFromString(new String(input.readAllBytes()));
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Register Events
        getServer().getPluginManager().registerEvents(new PhysicsListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
