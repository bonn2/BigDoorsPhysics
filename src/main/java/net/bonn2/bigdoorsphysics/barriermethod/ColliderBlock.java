package net.bonn2.bigdoorsphysics.barriermethod;

import net.bonn2.bigdoorsphysics.util.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ColliderBlock {
    public final Location location;
    public final Location direction;
    private BlockData oldBlock;

    /**
     * @param location The position to place the block
     * @param direction A location that describes the direction that the block should try to push the player
     *                  <br>It will attempt to push the player horizontally first, and after it pushes the player in one direction it will stop trying to move them.</br>
     */
    public ColliderBlock(Location location, Location direction) {
        this.location = location;
        this.direction = direction;
    }

    public void place() {
        oldBlock = location.getWorld().getBlockAt(location).getBlockData();
        if (location.getWorld().getBlockAt(location).getType().isSolid()) return;
        location.getWorld().getBlockAt(location).setBlockData(Material.BARRIER.createBlockData());
        if (Config.hideBarriers()) {
            // Hide block from distant players
            // Todo: use protocol lib for this to optimize the packets
            for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
                if (!player.getWorld().equals(location.getWorld())) continue;
                double distance = location.distance(player.getLocation());
                if (distance > 3 && distance < 128)
                    player.sendBlockChange(location, Material.AIR.createBlockData());
            }
        }
        if (Objects.equals(direction, new Location(location.getWorld(), 0, 0, 0))) return;
        // Handle clipping players
        for (Player player : location.getWorld().getPlayers()) {
            if (new BoundingBox(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    location.getBlockX() + 1,
                    location.getBlockY() + 1,
                    location.getBlockZ() + 1
            ).overlaps(player.getBoundingBox())
            && !location.getWorld().getBlockAt(location.clone().add(direction)).getType().isSolid()) {
                player.teleport(player.getLocation().clone().add(direction));
            }
        }
    }

    public void remove() {
        if (location.getBlock().getType() == Material.BARRIER && !oldBlock.getMaterial().isSolid())
            location.getWorld().getBlockAt(location).setBlockData(oldBlock);
    }
}
