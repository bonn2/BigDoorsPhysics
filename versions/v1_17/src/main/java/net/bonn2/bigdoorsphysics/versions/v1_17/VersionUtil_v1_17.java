package net.bonn2.bigdoorsphysics.versions.v1_17;

import net.bonn2.bigdoorsphysics.versions.VersionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class VersionUtil_v1_17 implements VersionUtil {

    @Override
    public boolean test() {
        try {
            Entity.class.getMethod("teleport", Location.class);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @Override
    public void teleportWithPassenger(Entity entity, Entity passenger, Location location) {
        entity.removePassenger(passenger);
        entity.teleport(location);
        entity.addPassenger(passenger);
    }
}
