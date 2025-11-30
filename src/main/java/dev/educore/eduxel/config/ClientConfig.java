package dev.educore.eduxel.config;

import dev.educore.eduxel.security.CryptoUtils;

import java.util.prefs.Preferences;

/**
 * Stores only broker configuration: server host, port and the encrypted secret.
 * No database credentials are persisted locally.
 */
public class ClientConfig {
    private static final String PREF_NODE = "dev.educore.eduxel.client";
    private static final String KEY_HOST = "broker.host";
    private static final String KEY_PORT = "broker.port";
    private static final String KEY_SECRET = "broker.secret.enc";

    private String host;
    private int port;
    private String secretEncrypted;

    public static ClientConfig load() {
        Preferences p = Preferences.userRoot().node(PREF_NODE);
        ClientConfig cfg = new ClientConfig();
        cfg.host = p.get(KEY_HOST, "");
        cfg.port = p.getInt(KEY_PORT, 0);
        cfg.secretEncrypted = p.get(KEY_SECRET, "");
        return cfg;
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREF_NODE);
        p.put(KEY_HOST, host == null ? "" : host);
        p.putInt(KEY_PORT, port);
        p.put(KEY_SECRET, secretEncrypted == null ? "" : secretEncrypted);
    }

    public boolean isConfigured() {
        return host != null && !host.isBlank() && port > 0 && secretEncrypted != null && !secretEncrypted.isBlank();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSecretEncrypted() {
        return secretEncrypted;
    }

    public void setSecretEncrypted(String secretEncrypted) {
        this.secretEncrypted = secretEncrypted;
    }

    public void setSecretPlain(String secretPlain) {
        this.secretEncrypted = CryptoUtils.encryptToBase64(secretPlain);
    }

    public String getSecretPlain() {
        return CryptoUtils.decryptFromBase64(secretEncrypted);
    }
}
