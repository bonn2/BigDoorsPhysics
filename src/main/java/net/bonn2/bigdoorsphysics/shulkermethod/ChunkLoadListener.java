package net.bonn2.bigdoorsphysics.shulkermethod;

import net.bonn2.bigdoorsphysics.BigDoorsPhysics;
import net.bonn2.bigdoorsphysics.util.Config;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        // Never run this method unless config is enabled
        if (!Config.removeShulkersOnChunkLoad()) return;
        // Remove all physics entities inside newly loaded chunk
        int count = 0;
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Shulker && Objects.equals(entity.getCustomName(), "BigDoorsPhysicsS")) {
                entity.remove();
                count++;
            }
            if (entity instanceof ArmorStand && Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) {
                entity.remove();
                count++;
            }
        }
        if (count > 0 && Config.detailedLogging())
            BigDoorsPhysics.PLUGIN.getLogger().info("Cleaned up " + count + " physics entities in chunk " + event.getChunk().getX() + " " + event.getChunk().getZ());
    }
}
