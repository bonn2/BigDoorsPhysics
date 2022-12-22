package net.bonn2.bigdoorsphysics;

import org.bukkit.Location;
import org.bukkit.Material;

public class ColliderBlock {
    public final Location location;

    public ColliderBlock(Location location) {
        this.location = location;
    }

    public void place() {
        location.getWorld().setBlockData(location, Material.BARRIER.createBlockData());
    }

    public void remove() {
        if (location.getBlock().getType() == Material.BARRIER)
            location.getWorld().setBlockData(location, Material.AIR.createBlockData());
    }
}
