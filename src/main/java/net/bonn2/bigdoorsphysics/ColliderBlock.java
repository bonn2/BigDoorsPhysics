package net.bonn2.bigdoorsphysics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ColliderBlock {
    public final Location location;
    public final Location direction;

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
        location.getWorld().setBlockData(location, Material.BARRIER.createBlockData());
        if (Objects.equals(direction, new Location(location.getWorld(), 0, 0, 0))) return;
        // Handle clipping players
        for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
            if (new BoundingBox(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    location.getBlockX() + 1,
                    location.getBlockY() + 1,
                    location.getBlockZ() + 1
            ).contains(
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ()
            )
            && !location.getWorld().getType(location.add(direction)).isCollidable()) {
                player.teleport(player.getLocation().add(direction));
            }
        }
    }

    public void remove() {
        if (location.getBlock().getType() == Material.BARRIER)
            location.getWorld().setBlockData(location, Material.AIR.createBlockData());
    }
}
