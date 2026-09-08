package me.pinkcandy.plugGet;

import me.pinkcandy.plugGet.api.modrinth.fetch.FetchProjects;
import me.pinkcandy.plugGet.commands.CommandsHandler;
import me.pinkcandy.plugGet.commands.Tab;
import me.pinkcandy.plugGet.db.RecreateFiles;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static me.pinkcandy.plugGet.db.DBManager.*;

public final class PlugGet extends JavaPlugin {

    public static PlugGet instance;
    public static Path dbFolder;
    public static Path dbFile;
    public static Path dbBackupFile;
    public static Path tmpFolder;
    public static Path plugincCacheFolder;
    public static Path projectCacheFolder;
    public static Path backupFolder;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigManager.reload(instance);
        RecreateFiles.recreateFiles();
        loadDB();
        loadBackupDB();
        new ServerInfo(instance);
        this.getCommand("plugget").setExecutor(new CommandsHandler());
        getCommand("plugget").setTabCompleter(new Tab());
        int pluginId = 31129;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("managed_plugins", () -> {

            return getInstalledPluginsCount();
        }));
        me.pinkcandy.plugGet.notifications.UpdateNotifier.init(this);
    }

    @Override
    public void onDisable() {
        ThreadManager.shutdown();
        try {
            if (Files.exists(tmpFolder)) {
                Files.walk(tmpFolder)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try { Files.delete(path); }
                            catch (Exception e) { e.printStackTrace(); }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
