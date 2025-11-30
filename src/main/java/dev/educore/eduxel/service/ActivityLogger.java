package dev.educore.eduxel.service;

import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class ActivityLogger {
    private ActivityLogger() {}

    public static void log(String area, String action, String user) {
        String sql = "INSERT INTO activities(area, action, user) VALUES (?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, area);
            ps.setString(2, action);
            ps.setString(3, user);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // best-effort logging; ignore errors for UI stability
        }
    }
}
