package me.pinkcandy.plugGet;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    public enum TabMode { APT, PACMAN }
    public enum SearchMode { FAST, FULL }
    public enum InstallFlag { LATEST, ROLLING, STABLE, BETA, ALPHA }

    // [root]
    public static TabMode tabMode;
    public static boolean serverConsoleOnly;

    // [search]
    public static SearchMode searchMode;
    public static int searchMaxResults;
    public static boolean searchReversedList;
    public static boolean showIncompatible;

    // [install]
    public static InstallFlag installDefaultFlag;
    public static boolean requiredDependencies;
    public static boolean optionalDependencies;

    // [cache]
    public static boolean cachePlugins;
    public static boolean cacheMetadata;
    public static boolean tmpVersions;

    // [backups]
    public static boolean backupsEnabled;
    public static int backupRetentionDays;

    // [notifications]
    public static int checkIntervalHours;
    public static boolean notifyOnJoin;

    public static void reload(PlugGet instance) {
        instance.saveDefaultConfig();
        instance.reloadConfig();
        FileConfiguration c = instance.getConfig();
        // [root]
        tabMode = parseEnum(c, "tab-mode", TabMode.class, TabMode.APT, instance);
        serverConsoleOnly = getBool(c, "server-console-only", false, instance);

        // [search]
        searchMode = parseEnum(c, "search.mode", SearchMode.class, SearchMode.FULL, instance);
        searchMaxResults = getInt(c, "search.max-results", 20, instance);
        searchReversedList = getBool(c, "search.reversed-list", true, instance);
        showIncompatible = getBool(c, "search.show-incompatible", false, instance);

        // [install]
        installDefaultFlag = parseEnum(c, "install.default-flag", InstallFlag.class, InstallFlag.LATEST, instance);
        requiredDependencies = getBool(c, "install.required-dependencies", true, instance);
        optionalDependencies = getBool(c, "install.optional-dependencies", false, instance);

        // [cache]
        cachePlugins = getBool(c, "cache.plugins", true, instance);
        cacheMetadata = getBool(c, "cache.metadata", true, instance);
        tmpVersions = getBool(c, "cache.tmp-versions", true, instance);

        // [backups]
        backupsEnabled = getBool(c, "backups.enabled", true, instance);
        backupRetentionDays = getInt(c, "backups.retention-days", 7, instance);

        // [notifications]
        checkIntervalHours = getInt(c, "notifications.check-interval-hours", 12, instance);
        notifyOnJoin = getBool(c, "notifications.notify-on-join", true, instance);
    }

    private static <E extends Enum<E>> E parseEnum(FileConfiguration c, String key, Class<E> type, E fallback, PlugGet plugin) {
        if (!c.contains(key)) {
            plugin.getLogger().warning(String.format(
                    "Missing config key '%s'. Using default: %s",
                    key, fallback.name().toLowerCase()
            ));
            return fallback;
        }
        String raw = c.getString(key);
        try {
            return Enum.valueOf(type, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(String.format(
                    "Unknown value '%s': \"%s\". Using default: %s",
                    key, raw, fallback.name().toLowerCase()
            ));
            return fallback;
        }
    }

    private static boolean getBool(FileConfiguration c, String key, boolean fallback, PlugGet plugin) {
        if (!c.contains(key)) {
            plugin.getLogger().warning(String.format(
                    "Missing config key '%s'. Using default: %s",
                    key, fallback
            ));
            return fallback;
        }
        return c.getBoolean(key, fallback);
    }

    private static int getInt(FileConfiguration c, String key, int fallback, PlugGet plugin) {
        if (!c.contains(key)) {
            plugin.getLogger().warning(String.format(
                    "Missing config key '%s'. Using default: %s",
                    key, fallback
            ));
            return fallback;
        }
        return c.getInt(key, fallback);
    }
}