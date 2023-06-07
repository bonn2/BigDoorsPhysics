package net.bonn2.bigdoorsphysics.versions;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class VersionUtilProvider {
    private static VersionUtil VERSION_UTIL = null;
    private static final Logger logger = Bukkit.getPluginManager().getPlugin("BigDoorsPhysics").getLogger();

    public static @Nullable VersionUtil getVersionUtil() {
        if (VERSION_UTIL != null) return VERSION_UTIL;
        Reflections reflections;
        if (Runtime.version().feature() < 17)
            reflections = new Reflections("net.bonn2.bigdoorsphysics.versions.j11");
        else
            reflections = new Reflections("net.bonn2.bigdoorsphysics.versions");
        Set<Class<? extends VersionUtil>> classes = reflections.getSubTypesOf(VersionUtil.class);
        logger.info("Discovered " + classes.size() + " version adapters");

        // Sort classes into order of most recent first
        Set<Class<? extends VersionUtil>> sortedClasses = new TreeSet<>(new VersionUtilComparator());
        sortedClasses.addAll(classes);

        // Test to see if class works, return first one that does
        for (Class<?> clazz : sortedClasses) {
            try {
                VersionUtil versionUtil = (VersionUtil) clazz.getConstructors()[0].newInstance();
                if (versionUtil.test()) {
                    logger.info("Loaded " + clazz.getName());
                    VERSION_UTIL = versionUtil;
                    return VERSION_UTIL;
                }
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {}
        }
        return null;
    }

    // Custom Comparator to compare class names based on version numbers
    private static class VersionUtilComparator implements Comparator<Class<?>> {
        @Override
        public int compare(@NotNull Class<?> clazz1, @NotNull Class<?> clazz2) {
            String version1 = getVersionNumber(clazz1.getSimpleName());
            String version2 = getVersionNumber(clazz2.getSimpleName());
            return compareVersions(version1, version2);
        }

        @Contract(pure = true)
        private @NotNull String getVersionNumber(@NotNull String className) {
            return className.replaceAll("[^0-9]+", "");
        }

        private int compareVersions(@NotNull String version1, @NotNull String version2) {
            String[] parts1 = version1.split("_");
            String[] parts2 = version2.split("_");

            for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);

                if (num1 < num2) {
                    return 1;
                } else if (num1 > num2) {
                    return -1;
                }
            }

            return Integer.compare(parts2.length, parts1.length); // Reverse the comparison result
        }
    }
}
