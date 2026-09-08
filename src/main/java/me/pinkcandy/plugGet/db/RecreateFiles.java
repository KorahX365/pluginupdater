package me.pinkcandy.plugGet.db;

import me.pinkcandy.plugGet.PlugGet;
import me.pinkcandy.plugGet.ServerInfo;
import me.pinkcandy.plugGet.install.InstallHelper;
import me.pinkcandy.plugGet.model.InstallInfo;
import me.pinkcandy.plugGet.model.PluginData;
import me.pinkcandy.plugGet.model.VersionInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static me.pinkcandy.plugGet.PlugGet.*;
import static me.pinkcandy.plugGet.db.DBManager.getPlugins;

public class RecreateFiles {
    public static void recreateFiles() {
        dbFolder = PlugGet.instance.getDataFolder().toPath().resolve("db/");
        dbFile = dbFolder.resolve("plugins.json");
        dbBackupFile = dbFolder.resolve("plugins_backup.json");
        tmpFolder = PlugGet.instance.getDataFolder().toPath().resolve("tmp/");
        plugincCacheFolder = PlugGet.instance.getDataFolder().toPath().resolve("cache/plugins/");
        projectCacheFolder = PlugGet.instance.getDataFolder().toPath().resolve("cache/project/");
        backupFolder = PlugGet.instance.getDataFolder().toPath().resolve("backups/");
        try {
            Files.createDirectories(dbFolder);

            if (!Files.exists(dbFile)) {
                JSONObject dbJson = new JSONObject();
                dbJson.put("plugins", new JSONObject());
                JSONObject plugins = getPlugins(dbJson);
                JSONObject pg = writeDefaultDB();
                for (String slug : pg.keySet()) {
                    JSONObject pluginObj = pg.getJSONObject(slug);
                    plugins.put(slug, pluginObj);
                }
                Files.write(
                        dbFile,
                        dbJson.toString(4).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE
                );
            }

            if (!Files.exists(dbBackupFile)) {
                JSONObject dbJson = new JSONObject();
                dbJson.put("plugins", new JSONObject());
                Files.write(
                        dbBackupFile,
                        dbJson.toString(4).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE
                );
            }

            Files.createDirectories(tmpFolder);
            Files.createDirectories(plugincCacheFolder);
            Files.createDirectories(projectCacheFolder);
            Files.createDirectories(backupFolder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject writeDefaultDB() {
        Path jarPath;
        int fileSize;
        String fileHash;
        try {
            jarPath = Paths.get(
                    instance.getClass()
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
            fileSize = Files.exists(jarPath) ? (int) Files.size(jarPath) : 67;
            InputStream is = Files.newInputStream(jarPath);
            fileHash = InstallHelper.sha512Hex(is);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        PluginData plugget = new PluginData(
                new InstallInfo(
                        "plug-get",
                        "latest",
                        "0.1.1"
                ),
                new VersionInfo(
                        "0.1.1",
                        "67",
                        "beta",
                        "listed",
                        "2026-06-14T16:05:27.268664Z",
                        null,
                        null,
                        null,
                        jarPath.getFileName().toString(),
                        "https://api.modrinth.com/v2/project/plugh-get/version/0.1.1",
                        fileHash,
                        fileSize,
                        null
                ),
                null
        );
        JSONObject pg = DBMapper.pluginToJson(plugget);
        return pg;
    }
}
