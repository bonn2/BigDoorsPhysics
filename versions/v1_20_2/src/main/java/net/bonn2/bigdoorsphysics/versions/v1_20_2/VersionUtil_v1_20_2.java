package net.bonn2.bigdoorsphysics.versions.v1_20_2;

import io.papermc.paper.entity.TeleportFlag;
import net.bonn2.bigdoorsphysics.versions.VersionUtil;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class VersionUtil_v1_20_2 implements VersionUtil {

    @Override
    public boolean test() {
        // Check if version is before 1.20.2
        if (new ComparableVersion(Bukkit.getMinecraftVersion()).compareTo(new ComparableVersion("1.20.2")) < 0) return false;
        // Check if server supports new teleport system
        try {
            Class.forName("io.papermc.paper.entity.TeleportFlag");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void teleportWithPassenger(Entity entity, Entity passenger, Location location) {
        entity.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }

    @Override
    public void setEntityInvisible(LivingEntity entity) {
        entity.setInvisible(true);
    }

    @Override
    public double getShulkerOffset() {
        return 0.9875;
    }
}
