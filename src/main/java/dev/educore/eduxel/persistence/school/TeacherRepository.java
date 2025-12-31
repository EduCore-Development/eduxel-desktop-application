package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.security.DataEncryptionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TeacherRepository {

    public long create(String firstName, String lastName, String subject) throws SQLException {
        String sql = "INSERT INTO teachers(first_name, last_name, subject) VALUES(?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, DataEncryptionService.encryptNullable(firstName));
            ps.setString(2, DataEncryptionService.encryptNullable(lastName));
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
            ps.setString(1, DataEncryptionService.encryptNullable(t.getFirstName()));
            ps.setString(2, DataEncryptionService.encryptNullable(t.getLastName()));
            if (t.getSubject() == null || t.getSubject().isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, t.getSubject());
            if (t.getEmail() == null || t.getEmail().isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, DataEncryptionService.encryptNullable(t.getEmail()));
            if (t.getPhone() == null || t.getPhone().isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, DataEncryptionService.encryptNullable(t.getPhone()));
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
            ps.setString(1, DataEncryptionService.encryptNullable(t.getFirstName()));
            ps.setString(2, DataEncryptionService.encryptNullable(t.getLastName()));
            if (t.getSubject() == null || t.getSubject().isBlank()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, t.getSubject());
            if (t.getEmail() == null || t.getEmail().isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, DataEncryptionService.encryptNullable(t.getEmail()));
            if (t.getPhone() == null || t.getPhone().isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, DataEncryptionService.encryptNullable(t.getPhone()));
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
                t.setFirstName(DataEncryptionService.decryptNullable(rs.getString("first_name")));
                t.setLastName(DataEncryptionService.decryptNullable(rs.getString("last_name")));
                t.setSubject(rs.getString("subject"));
                t.setEmail(DataEncryptionService.decryptNullable(rs.getString("email")));
                t.setPhone(DataEncryptionService.decryptNullable(rs.getString("phone")));
                return Optional.of(t);
            }
        }
    }

    public List<TeacherItem> listAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name FROM teachers";
        List<TeacherItem> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String first = DataEncryptionService.decryptNullable(rs.getString("first_name"));
                String last = DataEncryptionService.decryptNullable(rs.getString("last_name"));
                String fullName = (last == null ? "" : last) + ", " + (first == null ? "" : first);
                list.add(new TeacherItem(id, fullName));
            }
        }
        // Sort in-memory by display name
        list.sort(Comparator.comparing(item -> item.displayName, String.CASE_INSENSITIVE_ORDER));
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

    /**
     * Sucht Lehrer nach Suchbegriff (Name, Fach, Email, etc.)
     */
    public List<Teacher> searchTeachers(String searchTerm, String subjectFilter, int limit) throws SQLException {
        String sql = "SELECT id, first_name, last_name, subject, email, phone FROM teachers";
        List<Teacher> results = new ArrayList<>();

        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next() && results.size() < limit) {
                Teacher t = extractTeacherFromResultSet(rs);

                // Client-side filtering
                if (matchesSearchTerm(t, searchTerm, subjectFilter)) {
                    results.add(t);
                }
            }
        }
        return results;
    }

    /**
     * Lädt alle Lehrer als vollständige Teacher-Objekte
     */
    public List<Teacher> findAll(int limit) throws SQLException {
        return searchTeachers(null, null, limit);
    }

    private boolean matchesSearchTerm(Teacher t, String searchTerm, String subjectFilter) {
        // Subject filter
        if (subjectFilter != null && !subjectFilter.isBlank()) {
            if (t.getSubject() == null || !t.getSubject().equalsIgnoreCase(subjectFilter)) {
                return false;
            }
        }

        // General search term
        if (searchTerm == null || searchTerm.isBlank()) return true;
        String lowerTerm = searchTerm.toLowerCase();

        return matches(t.getFirstName(), lowerTerm) ||
               matches(t.getLastName(), lowerTerm) ||
               matches(t.getSubject(), lowerTerm) ||
               matches(t.getEmail(), lowerTerm) ||
               matches(t.getPhone(), lowerTerm);
    }

    private boolean matches(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private Teacher extractTeacherFromResultSet(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getLong("id"));
        t.setFirstName(DataEncryptionService.decryptNullable(rs.getString("first_name")));
        t.setLastName(DataEncryptionService.decryptNullable(rs.getString("last_name")));
        t.setSubject(rs.getString("subject"));
        t.setEmail(DataEncryptionService.decryptNullable(rs.getString("email")));
        t.setPhone(DataEncryptionService.decryptNullable(rs.getString("phone")));
        return t;
    }

    /**
     * Löscht einen Lehrer
     */
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM teachers";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
