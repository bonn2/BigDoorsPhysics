package net.bonn2.bigdoorsphysics.versions.v1_19_3;

import io.papermc.paper.entity.TeleportFlag;
import net.bonn2.bigdoorsphysics.versions.VersionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class VersionUtil_v1_19_3 implements VersionUtil {

    @Override
    public boolean test() {
        try {
            Class.forName("io.papermc.paper.entity.TeleportFlag");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void teleportWithPassenger(Entity entity, Entity passenger, Location location) {
        //noinspection UnstableApiUsage
        entity.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }
}
