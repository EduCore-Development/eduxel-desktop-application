package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.*;

public class StudentRepository {

    public long create(String firstName, String lastName, Long classId) throws SQLException {
        String sql = "INSERT INTO students(first_name, last_name, class_id) VALUES(?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            if (classId == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, classId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public void assignToClass(long studentId, long classId) throws SQLException {
        String sql = "UPDATE students SET class_id = ? WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setLong(2, studentId);
            ps.executeUpdate();
        }
    }
}
