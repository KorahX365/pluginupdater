package me.pinkcandy.plugGet.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.bukkit.command.CommandSender;

import static me.pinkcandy.plugGet.PlugGet.plugincCacheFolder;
import static me.pinkcandy.plugGet.PlugGet.tmpFolder;
public class FileDownloader {

    public static boolean downloadFile(String urlString, String fileName, String versionNumber, String slug, CommandSender sender) {


        Path targetFile = tmpFolder.resolve(fileName);

        try {
            Path cache = plugincCacheFolder.resolve(slug + "/" + versionNumber + "/" + fileName);

            if (Files.exists(cache))
            {
                Files.copy(cache, tmpFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }

            Files.createDirectories(tmpFolder);

            if (Files.exists(targetFile)) {
                Files.delete(targetFile);
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", me.pinkcandy.plugGet.api.HttpUtils.USER_AGENT);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
                    String redirectUrl = conn.getHeaderField("Location");
                    if (redirectUrl != null) {
                        conn.disconnect();
                        url = new URL(redirectUrl);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", me.pinkcandy.plugGet.api.HttpUtils.USER_AGENT);
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(30000);
                        responseCode = conn.getResponseCode();
                    }
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    sender.sendMessage("§cServer returned HTTP error " + responseCode + " for download.");
                    return false;
                }

                try (java.io.InputStream in = conn.getInputStream()) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                if (conn != null) conn.disconnect();
            }

            return true;

        } catch (UnknownHostException e) {

            sender.sendMessage("§cNo internet connection or DNS lookup failed.");

        } catch (SocketTimeoutException e) {

            sender.sendMessage("§cConnection timed out. The server did not respond.");

        } catch (MalformedURLException e) {

            sender.sendMessage("§cInvalid download URL.");

        } catch (FileNotFoundException e) {

            sender.sendMessage("§cFile not found (404).");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}



