package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.*;

public class TeacherRepository {

    public long create(String firstName, String lastName, String subject) throws SQLException {
        String sql = "INSERT INTO teachers(first_name, last_name, subject) VALUES(?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            if (subject == null || subject.isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, subject);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }
}
