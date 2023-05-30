package net.bonn2.bigdoorsphysics.util;

import net.bonn2.bigdoorsphysics.versions.VersionUtil;

import java.util.Arrays;
import java.util.Locale;

public enum CollisionMethod {
        SHULKER, BARRIER, NONE;

        public static CollisionMethod safeValueOf(String string) {
                if (Arrays.stream(values()).anyMatch(value -> value.name().equals(string.toUpperCase(Locale.ROOT))))
                        return valueOf(string.toUpperCase(Locale.ROOT));
                else
                        return NONE;
        }

        public static CollisionMethod getDefaultValue() {
                if (VersionUtil.getMajorVersion() <= 19 || VersionUtil.isSpigot())
                        return BARRIER;
                else
                        return SHULKER;
        }
}
