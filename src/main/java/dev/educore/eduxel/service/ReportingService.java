package dev.educore.eduxel.service;

import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.ui.main.ActivityEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportingService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.y HH:mm").withZone(ZoneId.systemDefault());

    public List<ActivityEntry> listRecentActivities(int limit) throws Exception {
        String sql = "SELECT time, area, action, user FROM activities ORDER BY time DESC LIMIT ?";
        List<ActivityEntry> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String time = TIME_FORMATTER.format(rs.getTimestamp(1).toInstant());
                    String area = rs.getString(2);
                    String action = rs.getString(3);
                    String user = rs.getString(4);
                    list.add(new ActivityEntry(time, area, action, user == null ? "" : user));
                }
            }
        }
        return list;
    }

    public Statistics getGlobalStatistics() throws Exception {
        Statistics stats = new Statistics();
        try (Connection con = DataSourceProvider.getConnection()) {
            stats.studentCount = getCount(con, "students");
            stats.teacherCount = getCount(con, "teachers");
            stats.classCount = getCount(con, "class_groups");
            stats.deviceCount = getCount(con, "devices");
            stats.webAccountCount = getCount(con, "web_accounts");
            
            // Sick/Missing
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM students WHERE is_sick = TRUE");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.sickStudentCount = rs.getInt(1);
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM students WHERE is_missing_unexcused = TRUE");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.missingStudentCount = rs.getInt(1);
            }
        }
        return stats;
    }

    private int getCount(Connection con, String table) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[Reporting] Fehler beim Abrufen der Anzahl für Tabelle '" + table + "': " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public static class Statistics {
        public int studentCount;
        public int teacherCount;
        public int classCount;
        public int deviceCount;
        public int webAccountCount;
        public int sickStudentCount;
        public int missingStudentCount;
    }
}
