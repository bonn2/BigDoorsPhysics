package net.bonn2.bigdoorsphysics.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class VersionUtils {

    static int majorVersion = Integer.parseInt(PLUGIN.getServer().getMinecraftVersion().split("\\.")[1]);

    public static boolean isCollidable(Material material) {
        if (majorVersion >= 17)
            return material.isCollidable();
        else
            return material.isAir();
    }

    public static void teleportWithPassenger(Entity entity, Entity passenger, Location location) {
        if (majorVersion >= 19) {
            entity.teleport(location, true);
        } else {
            entity.removePassenger(passenger);
            entity.teleport(location);
            entity.addPassenger(passenger);
        }
    }
}
