package me.pinkcandy.plugGet.notifications;

import me.pinkcandy.plugGet.ConfigManager;
import me.pinkcandy.plugGet.PlugGet;
import me.pinkcandy.plugGet.ThreadManager;
import me.pinkcandy.plugGet.Update.UpdatePreparer;
import me.pinkcandy.plugGet.backup.BackupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateNotifier implements Listener {

    private static final List<String> cachedUpdatablePlugins = new CopyOnWriteArrayList<>();
    private static BukkitTask periodicTask = null;

    public static void init(PlugGet plugin) {
        Bukkit.getPluginManager().registerEvents(new UpdateNotifier(), plugin);

        schedulePeriodicCheck(plugin);
    }

    public static void schedulePeriodicCheck(PlugGet plugin) {
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }

        long intervalHours = ConfigManager.checkIntervalHours;
        if (intervalHours <= 0) {
            return;
        }

        // 20 ticks * 60 seconds * 60 minutes * intervalHours
        long intervalTicks = 20L * 60L * 60L * intervalHours;
        long initialDelayTicks = 20L * 30L; // Start first check 30s after server load

        periodicTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            checkForUpdates(true);
            BackupManager.cleanOldBackups();
        }, initialDelayTicks, intervalTicks);
    }

    public static void checkForUpdates(boolean notify) {
        ThreadManager.runAsync(() -> {
            List<String> updatable = UpdatePreparer.getAvailableUpdatesList();
            cachedUpdatablePlugins.clear();
            cachedUpdatablePlugins.addAll(updatable);

            if (notify && !updatable.isEmpty()) {
                String message = "§8[§dPlug-Get§8] §eThere are §a" + updatable.size() +
                        " §eplugin update(s) available: §7" + String.join(", ", updatable) +
                        "§e. Use §d/pg update §eto review and update.";

                Bukkit.getConsoleSender().sendMessage(message);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("plugget.notify") || player.hasPermission("plugget.use") || player.isOp()) {
                        player.sendMessage(message);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.notifyOnJoin) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("plugget.notify") && !player.hasPermission("plugget.use") && !player.isOp()) {
            return;
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(PlugGet.instance, () -> {
            if (!player.isOnline()) return;

            if (cachedUpdatablePlugins.isEmpty()) {
                List<String> updatable = UpdatePreparer.getAvailableUpdatesList();
                cachedUpdatablePlugins.clear();
                cachedUpdatablePlugins.addAll(updatable);
            }

            if (!cachedUpdatablePlugins.isEmpty()) {
                player.sendMessage("§8[§dPlug-Get§8] §eThere are §a" + cachedUpdatablePlugins.size() +
                        " §eplugin update(s) available: §7" + String.join(", ", cachedUpdatablePlugins) +
                        "§e. Run §d/pg update §eto update them.");
            }
        }, 60L); // 3 seconds after join
    }
}
