package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public long createFull(Teacher t) throws SQLException {
        String sql = "INSERT INTO teachers(first_name, last_name, subject, email, phone) VALUES(?,?,?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getFirstName());
            ps.setString(2, t.getLastName());
            if (t.getSubject() == null || t.getSubject().isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, t.getSubject());
            if (t.getEmail() == null || t.getEmail().isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, t.getEmail());
            if (t.getPhone() == null || t.getPhone().isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, t.getPhone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public void update(Teacher t) throws SQLException {
        String sql = "UPDATE teachers SET first_name = ?, last_name = ?, subject = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getFirstName());
            ps.setString(2, t.getLastName());
            if (t.getSubject() == null || t.getSubject().isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, t.getSubject());
            if (t.getEmail() == null || t.getEmail().isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, t.getEmail());
            if (t.getPhone() == null || t.getPhone().isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, t.getPhone());
            ps.setLong(6, t.getId());
            ps.executeUpdate();
        }
    }

    public Optional<Teacher> findById(long id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, subject, email, phone FROM teachers WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Teacher t = new Teacher();
                t.setId(rs.getLong("id"));
                t.setFirstName(rs.getString("first_name"));
                t.setLastName(rs.getString("last_name"));
                t.setSubject(rs.getString("subject"));
                t.setEmail(rs.getString("email"));
                t.setPhone(rs.getString("phone"));
                return Optional.of(t);
            }
        }
    }

    public List<TeacherItem> listAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, subject FROM teachers ORDER BY last_name, first_name";
        List<TeacherItem> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String fullName = (last == null ? "" : last) + ", " + (first == null ? "" : first);
                list.add(new TeacherItem(id, fullName));
            }
        }
        return list;
    }

    public static final class TeacherItem {
        public final long id;
        public final String displayName;

        public TeacherItem(long id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override public String toString() { return displayName; }
    }
}
