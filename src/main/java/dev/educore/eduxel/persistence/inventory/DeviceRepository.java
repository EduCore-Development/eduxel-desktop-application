package dev.educore.eduxel.persistence.inventory;

import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.*;

public class DeviceRepository {

    public long create(String assetTag, String type, String status) throws SQLException {
        String sql = "INSERT INTO devices(asset_tag, type, status) VALUES(?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, assetTag);
            if (type == null || type.isBlank()) ps.setNull(2, Types.VARCHAR); else ps.setString(2, type);
            if (status == null || status.isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }
}
