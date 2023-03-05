package net.bonn2.bigdoorsphysics.versions.v1_19;

import net.bonn2.bigdoorsphysics.versions.VersionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;

public class VersionUtil_v1_19 implements VersionUtil {

    @Override
    public boolean test() {
        for (Method method : Entity.class.getMethods()) {
            if (method.getName().equals("teleport")
                    && method.getParameterTypes().length == 2
                    && method.getParameterTypes()[0].equals(Location.class)
                    && method.getParameterTypes()[1].equals(boolean.class))
                return true;
        }
        return false;
    }

    @Override
    public void teleportWithPassenger(Entity entity, Entity passenger, Location location) {
        //noinspection UnstableApiUsage
        entity.teleport(location, true);
    }
}
