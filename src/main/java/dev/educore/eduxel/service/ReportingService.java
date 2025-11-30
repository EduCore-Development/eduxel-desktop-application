package dev.educore.eduxel.service;

import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.ui.main.ActivityEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
}
