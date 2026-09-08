package me.pinkcandy.plugGet.backup;

import me.pinkcandy.plugGet.ConfigManager;
import me.pinkcandy.plugGet.PlugGet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static me.pinkcandy.plugGet.PlugGet.backupFolder;

public class BackupManager {

    public static void backupFile(Path sourceFile, String slug) {
        if (!ConfigManager.backupsEnabled || sourceFile == null || !Files.exists(sourceFile)) {
            return;
        }

        try {
            Path slugBackupFolder = backupFolder.resolve(slug);
            Files.createDirectories(slugBackupFolder);

            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalName = sourceFile.getFileName().toString();
            String backupName = timestamp + "_" + originalName;

            Path target = slugBackupFolder.resolve(backupName);
            Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
            PlugGet.instance.getLogger().info("[Plug-Get] Backed up " + originalName + " to " + target);
        } catch (Exception e) {
            PlugGet.instance.getLogger().warning("[Plug-Get] Could not create backup for " + sourceFile + ": " + e.getMessage());
        }
    }

    public static void cleanOldBackups() {
        if (backupFolder == null || !Files.exists(backupFolder)) {
            return;
        }

        int retentionDays = ConfigManager.backupRetentionDays;
        if (retentionDays <= 0) {
            return;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        try (Stream<Path> stream = Files.walk(backupFolder)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    Instant lastModified = attrs.lastModifiedTime().toInstant();
                    if (lastModified.isBefore(cutoff)) {
                        Files.deleteIfExists(file);
                        PlugGet.instance.getLogger().info("[Plug-Get] Cleaned expired backup: " + file.getFileName());
                    }
                } catch (IOException ignored) {}
            });
        } catch (Exception e) {
            PlugGet.instance.getLogger().warning("[Plug-Get] Error while cleaning old backups: " + e.getMessage());
        }
    }
}
