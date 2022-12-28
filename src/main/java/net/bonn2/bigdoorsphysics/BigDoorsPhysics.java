package net.bonn2.bigdoorsphysics;

import net.bonn2.bigdoorsphysics.barriermethod.ColliderBlock;
import net.bonn2.bigdoorsphysics.barriermethod.BarrierListener;
import net.bonn2.bigdoorsphysics.shulkermethod.ColliderShulker;
import net.bonn2.bigdoorsphysics.shulkermethod.ShulkerListener;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;
    public static YamlConfiguration CONFIG = new YamlConfiguration();

    private BarrierListener barrierListener;
    private ShulkerListener shulkerListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;

        getLogger().info("Loading Config");
        // Load config
        File configFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml");
        try {
            if (getDataFolder().mkdirs()) getLogger().info("Created data folder");
            if (configFile.createNewFile()) {
                try (FileOutputStream output = new FileOutputStream(configFile)) {
                    output.write(
                                    """
                                    ### BigDoorsPhysics Config
                                    
                                    # How the plugin should create the colliders
                                    # Valid options: BARRIER, SHULKER
                                    # Any invalid selection will just disable collisions
                                    method: SHULKER
                                    
                                    # Use extra packets to hide barriers when they are far away
                                    # This may have a medium performance impact, primarily fps
                                    hide-barriers: true
                                    
                                    # Move players who are near the door in accordance with the direction of the door
                                    # This may have a small performance impact, primarily tps
                                    move-players: true
                                    
                                    # Protect portcullises from self destruction
                                    # This prevents the plugin from operating on portcullises that are either one block wide or deep
                                    # This option will be removed when the bug is resolved
                                    # Disabling this option will likely make these doors delete their blocks when they are closed
                                    protect-portcullises: true
                                    
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
        getLogger().info("Registering Events");
        switch (Objects.requireNonNull(CONFIG.getString("method"))) {
            case "BARRIER" -> {
                barrierListener = new BarrierListener();
                getServer().getPluginManager().registerEvents(barrierListener, this);
            }
            case "SHULKER" -> {
                shulkerListener = new ShulkerListener();
                getServer().getPluginManager().registerEvents(shulkerListener, this);
            }
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
