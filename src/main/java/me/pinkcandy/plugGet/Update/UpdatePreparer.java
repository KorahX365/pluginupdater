package me.pinkcandy.plugGet.Update;

import me.pinkcandy.plugGet.api.modrinth.fetch.FetchHelper;
import me.pinkcandy.plugGet.commands.ActionLock;
import me.pinkcandy.plugGet.db.DBManager;
import me.pinkcandy.plugGet.install.InstallPlugins;
import me.pinkcandy.plugGet.messagesBuilders.BuildUpdateInfo;
import me.pinkcandy.plugGet.model.InstallInfo;
import me.pinkcandy.plugGet.model.PluginData;
import me.pinkcandy.plugGet.model.ProjectMeta;
import me.pinkcandy.plugGet.model.VersionInfo;
import me.pinkcandy.plugGet.version.CompareDate;
import me.pinkcandy.plugGet.version.GetNewestVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UpdatePreparer {
    public static boolean execute(CommandSender sender) {
        return execute(sender, null);
    }

    public static boolean execute(CommandSender sender, String targetSlug) {
        List<PluginData> pluginsInDB = new ArrayList<>();
        if (targetSlug != null && !targetSlug.trim().isEmpty()) {
            PluginData singleData = DBManager.getPluginData(targetSlug);
            if (singleData == null) {
                sender.sendMessage("§cPlugin " + targetSlug + " is not installed or registered in the database.");
                ActionLock.release();
                return false;
            }
            pluginsInDB.add(singleData);
            sender.sendMessage("§8:: §7Checking updates for §8" + targetSlug + "§7...");
        } else {
            pluginsInDB = DBManager.getInstalledPlugins();
            sender.sendMessage("§8:: §7Fetching updates for §8" + pluginsInDB.size() + " §7plugins...");
        }
        List<PluginData> installedPlugins = new ArrayList<>();
        List<PluginData> pluginsToUpdate = new ArrayList<>();
        int excludedTotalCount = 0;
        int excludedToUpdateCount = 0;
        for (int i = 0; i < pluginsInDB.size(); i++) {
            InstallInfo installInfo = pluginsInDB.get(i).getInstallInfo();
            boolean excluded = DBManager.isPluginExcluded(installInfo.getSlug());
            if (excluded) {
                excludedTotalCount++;
            }
            VersionInfo currentV = pluginsInDB.get(i).getVersionInfo();
            String slug = installInfo.getSlug();
            ProjectMeta meta = FetchHelper.getProject(slug);
            if (meta == null) {
                sender.sendMessage("§cInstalled plugin " + slug + " haven't been found on Modrinth. Does it still exist or mabey chainged the name?");
                continue;
            }

            List<VersionInfo> versions = new ArrayList<>();
            VersionInfo newestV = GetNewestVersion.getNewestVersionForInstallType(installInfo);
            if (newestV == null) {continue;}
            versions.add(newestV);
            versions.add(currentV);
            newestV =  CompareDate.compare(versions);
            if (newestV.getVersionNumber().equals(currentV.getVersionNumber()))
            {
                continue;
            }
            if (DBManager.isPluginExcluded(installInfo.getSlug())) {
                excludedToUpdateCount++;
                continue;
            }
            pluginsToUpdate.add(new PluginData(pluginsInDB.get(i).getInstallInfo(), newestV, "dif"));
            installedPlugins.add(new PluginData(pluginsInDB.get(i).getInstallInfo(), currentV, null));
        }
        if (!installedPlugins.isEmpty() && !pluginsToUpdate.isEmpty()) {
            List<BaseComponent[]> messages = BuildUpdateInfo.buildUpdateInfo(installedPlugins, pluginsToUpdate, excludedTotalCount, excludedToUpdateCount);
            ActionLock.isConfirming = true;
            for (int i = 0; i < messages.size(); i++) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.spigot().sendMessage(messages.get(i));
                } else {
                    sender.sendMessage(BaseComponent.toLegacyText(messages.get(i)));
                }
            }
        }
        else {
            sender.sendMessage("§aAll plugins are up to date!");
            ActionLock.release();
            return true;
        }

        ActionLock.confirm= () -> {
            boolean success = InstallPlugins.installPlugins(pluginsToUpdate, sender);
            ActionLock.release();
        };
        ActionLock.deny = () -> {
            sender.sendMessage("§cUpdate cancelled.");
            ActionLock.release();
        };
        return true;
    }

    public static List<String> getAvailableUpdatesList() {
        List<PluginData> pluginsInDB = DBManager.getInstalledPlugins();
        List<String> updatableSlugs = new ArrayList<>();
        for (PluginData pd : pluginsInDB) {
            InstallInfo installInfo = pd.getInstallInfo();
            if (DBManager.isPluginExcluded(installInfo.getSlug())) {
                continue;
            }
            VersionInfo currentV = pd.getVersionInfo();
            VersionInfo newestV = GetNewestVersion.getNewestVersionForInstallType(installInfo);
            if (newestV == null) continue;

            List<VersionInfo> versions = new ArrayList<>();
            versions.add(newestV);
            versions.add(currentV);
            newestV = CompareDate.compare(versions);

            if (newestV != null && !newestV.getVersionNumber().equals(currentV.getVersionNumber())) {
                updatableSlugs.add(installInfo.getSlug());
            }
        }
        return updatableSlugs;
    }
}
