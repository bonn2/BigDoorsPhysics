package net.bonn2.bigdoorsphysics.versions;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
// todo: make sure spigot versions are always less than standard versions
        @Contract(pure = true)
        private @NotNull String getVersionNumber(@NotNull String className) {
            return className.replaceAll("VersionUtil_v", "");
        }

        private int compareVersions(@NotNull String version1, @NotNull String version2) {
            List<String> parts1 = new ArrayList<>(List.of(version1.split("_")));
            List<String> parts2 = new ArrayList<>(List.of(version2.split("_")));

            // Add .0 to the end of two digit version numbers
            if (parts1.size() == 2) parts1.add("0");
            if (parts2.size() == 2) parts2.add("0");

            // Set last version point to 0 or 1 depending on if server only support spigot level api
            if (Objects.equals(parts1.get(parts1.size() - 1), "spigot")) parts1.set(parts1.size() - 1, "0");
            else parts1.add("1");
            if (Objects.equals(parts2.get(parts2.size() - 1), "spigot")) parts2.set(parts2.size() - 1, "0");
            else parts2.add("1");

            for (int i = 0; i < Math.min(parts1.size(), parts2.size()); i++) {
                int num1 = Integer.parseInt(parts1.get(i));
                int num2 = Integer.parseInt(parts2.get(i));

                if (num1 < num2) {
                    return 1;
                } else if (num1 > num2) {
                    return -1;
                }
            }
            return 0;
        }
    }
}
