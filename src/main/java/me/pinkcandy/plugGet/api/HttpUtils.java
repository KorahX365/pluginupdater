package me.pinkcandy.plugGet.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HttpUtils {

    public static final String USER_AGENT = "KorahX365/pluginupdater/0.1.1 (https://github.com/KorahX365/pluginupdater)";

    public static String get(String urlStr) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int status = conn.getResponseCode();

            if (status != 200) return null;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}