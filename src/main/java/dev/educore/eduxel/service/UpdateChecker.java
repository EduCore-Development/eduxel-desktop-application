package dev.educore.eduxel.service;

import dev.educore.eduxel.meta.EduxelMeta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private static final String VERSION_URL = "https://edu-core.dev/version.txt";
    private static final int TIMEOUT = 5000; // 5 seconds

    public static class VersionInfo {
        private final String appName;
        private final String version;
        private final String buildChannel;

        public VersionInfo(String appName, String version, String buildChannel) {
            this.appName = appName;
            this.version = version;
            this.buildChannel = buildChannel;
        }

        public String getAppName() {
            return appName;
        }

        public String getVersion() {
            return version;
        }

        public String getBuildChannel() {
            return buildChannel;
        }

        @Override
        public String toString() {
            return "VersionInfo{" +
                    "appName='" + appName + '\'' +
                    ", version='" + version + '\'' +
                    ", buildChannel='" + buildChannel + '\'' +
                    '}';
        }
    }

    /**
     * Prüft asynchron, ob ein Update verfügbar ist.
     * @return CompletableFuture mit true wenn Update verfügbar, false wenn nicht, oder Exception bei Fehler
     */
    public static CompletableFuture<Boolean> checkForUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VersionInfo remoteVersion = fetchRemoteVersion();
                VersionInfo currentVersion = getCurrentVersion();

                return !isVersionEqual(currentVersion, remoteVersion);
            } catch (Exception e) {
                System.err.println("Fehler beim Prüfen auf Updates: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Holt die Remote-Version von der URL.
     */
    public static VersionInfo fetchRemoteVersion() throws Exception {
        URI uri = new URI(VERSION_URL);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("User-Agent", "Eduxel Update Checker");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP Fehler: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        return parseVersionInfo(response.toString());
    }

    /**
     * Parst die Version-Informationen aus dem Text.
     * Erwartet Format:
     * appName=Eduxel
     * version=1.1.0
     * buildChannel=Release
     */
    private static VersionInfo parseVersionInfo(String text) throws Exception {
        String appName = null;
        String version = null;
        String buildChannel = null;

        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                continue;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            switch (key.toLowerCase()) {
                case "appname":
                    appName = value;
                    break;
                case "version":
                    version = value;
                    break;
                case "buildchannel":
                    buildChannel = value;
                    break;
            }
        }

        if (appName == null || version == null || buildChannel == null) {
            throw new Exception("Unvollständige Versionsinformationen");
        }

        return new VersionInfo(appName, version, buildChannel);
    }

    /**
     * Gibt die aktuelle Version der Anwendung zurück.
     */
    public static VersionInfo getCurrentVersion() {
        return new VersionInfo(
                EduxelMeta.APP_NAME,
                EduxelMeta.VERSION,
                EduxelMeta.BUILD_CHANNEL
        );
    }

    /**
     * Vergleicht zwei Versionen.
     */
    private static boolean isVersionEqual(VersionInfo v1, VersionInfo v2) {
        return v1.getAppName().equals(v2.getAppName()) &&
               v1.getVersion().equals(v2.getVersion()) &&
               v1.getBuildChannel().equals(v2.getBuildChannel());
    }
}

