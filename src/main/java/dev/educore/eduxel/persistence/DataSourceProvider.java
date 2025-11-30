package dev.educore.eduxel.persistence;

import dev.educore.eduxel.config.ClientConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DataSourceProvider {
    private static volatile DbCredentials cachedCreds;
    private static final Object LOCK = new Object();

    private static String jdbcUrl(DbCredentials c) {
        return "jdbc:mariadb://" + c.host + ":" + c.port + "/" + c.database + "?useUnicode=true&characterEncoding=utf8";
    }

    private static DbCredentials ensureCredentials() throws SQLException {
        if (cachedCreds != null) return cachedCreds;
        synchronized (LOCK) {
            if (cachedCreds == null) {
                ClientConfig cfg = ClientConfig.load();
                if (!cfg.isConfigured()) {
                    throw new SQLException("Eduxel Server (IP, Port, Secret) nicht konfiguriert");
                }
                CredentialBrokerClient client = new CredentialBrokerClient();
                try {
                    cachedCreds = client.fetchCredentials(cfg);
                } catch (Exception e) {
                    throw new SQLException("Fehler beim Abrufen der DB-Zugangsdaten: " + e.getMessage(), e);
                }
            }
        }
        return Objects.requireNonNull(cachedCreds);
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC-Treiber nicht gefunden", e);
        }
        DbCredentials c = ensureCredentials();
        return DriverManager.getConnection(jdbcUrl(c), c.user, c.password);
    }

    /**
     * Clears any cached database credentials. The next call to getConnection() will
     * fetch fresh credentials via the broker using the current ClientConfig.
     */
    public static void reset() {
        synchronized (LOCK) {
            cachedCreds = null;
        }
    }
}
