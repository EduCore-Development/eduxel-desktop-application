package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.security.DataEncryptionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public long create(String firstName, String lastName, Long classId) throws SQLException {
        String sql = "INSERT INTO students(first_name, last_name, class_id) VALUES(?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, DataEncryptionService.encryptNullable(firstName));
            ps.setString(2, DataEncryptionService.encryptNullable(lastName));
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

    /**
     * Legt einen Schüler mit allen Detailfeldern an.
     */
    public long createFull(Student s) throws SQLException {
        String sql = "INSERT INTO students(" +
                "first_name, last_name, class_id, " +
                "street, postal_code, city, country, " +
                "student_email, student_mobile, " +
                "guardian1_name, guardian1_relation, guardian1_mobile, guardian1_work_phone, guardian1_email, " +
                "guardian2_name, guardian2_relation, guardian2_mobile, guardian2_email, " +
                "notes, is_sick, is_missing_unexcused" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, DataEncryptionService.encryptNullable(s.getFirstName()));
            ps.setString(2, DataEncryptionService.encryptNullable(s.getLastName()));
            if (s.getClassId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, s.getClassId());
            ps.setString(4, DataEncryptionService.encryptNullable(s.getStreet()));
            ps.setString(5, DataEncryptionService.encryptNullable(s.getPostalCode()));
            ps.setString(6, DataEncryptionService.encryptNullable(s.getCity()));
            ps.setString(7, DataEncryptionService.encryptNullable(s.getCountry()));

            ps.setString(8, DataEncryptionService.encryptNullable(s.getStudentEmail()));
            ps.setString(9, DataEncryptionService.encryptNullable(s.getStudentMobile()));

            ps.setString(10, DataEncryptionService.encryptNullable(s.getGuardian1Name()));
            ps.setString(11, DataEncryptionService.encryptNullable(s.getGuardian1Relation()));
            ps.setString(12, DataEncryptionService.encryptNullable(s.getGuardian1Mobile()));
            ps.setString(13, DataEncryptionService.encryptNullable(s.getGuardian1WorkPhone()));
            ps.setString(14, DataEncryptionService.encryptNullable(s.getGuardian1Email()));

            ps.setString(15, DataEncryptionService.encryptNullable(s.getGuardian2Name()));
            ps.setString(16, DataEncryptionService.encryptNullable(s.getGuardian2Relation()));
            ps.setString(17, DataEncryptionService.encryptNullable(s.getGuardian2Mobile()));
            ps.setString(18, DataEncryptionService.encryptNullable(s.getGuardian2Email()));

            ps.setString(19, DataEncryptionService.encryptNullable(s.getNotes()));
            ps.setBoolean(20, s.isSick());
            ps.setBoolean(21, s.isMissingUnexcused());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET " +
                "first_name = ?, last_name = ?, class_id = ?, " +
                "street = ?, postal_code = ?, city = ?, country = ?, " +
                "student_email = ?, student_mobile = ?, " +
                "guardian1_name = ?, guardian1_relation = ?, guardian1_mobile = ?, guardian1_work_phone = ?, guardian1_email = ?, " +
                "guardian2_name = ?, guardian2_relation = ?, guardian2_mobile = ?, guardian2_email = ?, " +
                "notes = ?, is_sick = ?, is_missing_unexcused = ? " +
                "WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, DataEncryptionService.encryptNullable(s.getFirstName()));
            ps.setString(2, DataEncryptionService.encryptNullable(s.getLastName()));
            if (s.getClassId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, s.getClassId());
            ps.setString(4, DataEncryptionService.encryptNullable(s.getStreet()));
            ps.setString(5, DataEncryptionService.encryptNullable(s.getPostalCode()));
            ps.setString(6, DataEncryptionService.encryptNullable(s.getCity()));
            ps.setString(7, DataEncryptionService.encryptNullable(s.getCountry()));

            ps.setString(8, DataEncryptionService.encryptNullable(s.getStudentEmail()));
            ps.setString(9, DataEncryptionService.encryptNullable(s.getStudentMobile()));

            ps.setString(10, DataEncryptionService.encryptNullable(s.getGuardian1Name()));
            ps.setString(11, DataEncryptionService.encryptNullable(s.getGuardian1Relation()));
            ps.setString(12, DataEncryptionService.encryptNullable(s.getGuardian1Mobile()));
            ps.setString(13, DataEncryptionService.encryptNullable(s.getGuardian1WorkPhone()));
            ps.setString(14, DataEncryptionService.encryptNullable(s.getGuardian1Email()));

            ps.setString(15, DataEncryptionService.encryptNullable(s.getGuardian2Name()));
            ps.setString(16, DataEncryptionService.encryptNullable(s.getGuardian2Relation()));
            ps.setString(17, DataEncryptionService.encryptNullable(s.getGuardian2Mobile()));
            ps.setString(18, DataEncryptionService.encryptNullable(s.getGuardian2Email()));

            ps.setString(19, DataEncryptionService.encryptNullable(s.getNotes()));
            ps.setBoolean(20, s.isSick());
            ps.setBoolean(21, s.isMissingUnexcused());
            ps.setLong(22, s.getId());
            ps.executeUpdate();
        }
    }

    public Student findById(long id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, class_id, " +
                "street, postal_code, city, country, " +
                "student_email, student_mobile, " +
                "guardian1_name, guardian1_relation, guardian1_mobile, guardian1_work_phone, guardian1_email, " +
                "guardian2_name, guardian2_relation, guardian2_mobile, guardian2_email, " +
                "notes, is_sick, is_missing_unexcused FROM students WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Student s = new Student();
                s.setId(rs.getLong("id"));
                s.setFirstName(DataEncryptionService.decryptNullable(rs.getString("first_name")));
                s.setLastName(DataEncryptionService.decryptNullable(rs.getString("last_name")));
                long classId = rs.getLong("class_id");
                s.setClassId(rs.wasNull() ? null : classId);
                s.setStreet(DataEncryptionService.decryptNullable(rs.getString("street")));
                s.setPostalCode(DataEncryptionService.decryptNullable(rs.getString("postal_code")));
                s.setCity(DataEncryptionService.decryptNullable(rs.getString("city")));
                s.setCountry(DataEncryptionService.decryptNullable(rs.getString("country")));

                s.setStudentEmail(DataEncryptionService.decryptNullable(rs.getString("student_email")));
                s.setStudentMobile(DataEncryptionService.decryptNullable(rs.getString("student_mobile")));

                s.setGuardian1Name(DataEncryptionService.decryptNullable(rs.getString("guardian1_name")));
                s.setGuardian1Relation(DataEncryptionService.decryptNullable(rs.getString("guardian1_relation")));
                s.setGuardian1Mobile(DataEncryptionService.decryptNullable(rs.getString("guardian1_mobile")));
                s.setGuardian1WorkPhone(DataEncryptionService.decryptNullable(rs.getString("guardian1_work_phone")));
                s.setGuardian1Email(DataEncryptionService.decryptNullable(rs.getString("guardian1_email")));

                s.setGuardian2Name(DataEncryptionService.decryptNullable(rs.getString("guardian2_name")));
                s.setGuardian2Relation(DataEncryptionService.decryptNullable(rs.getString("guardian2_relation")));
                s.setGuardian2Mobile(DataEncryptionService.decryptNullable(rs.getString("guardian2_mobile")));
                s.setGuardian2Email(DataEncryptionService.decryptNullable(rs.getString("guardian2_email")));

                s.setNotes(DataEncryptionService.decryptNullable(rs.getString("notes")));
                return s;
            }
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Sucht Schüler nach Suchbegriff (Name, Notizen, etc.)
     * Da Daten verschlüsselt sind, werden alle Datensätze geladen und clientseitig gefiltert.
     */
    public List<Student> searchStudents(String searchTerm, Long filterClassId, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, first_name, last_name, class_id, " +
                "street, postal_code, city, country, " +
                "student_email, student_mobile, " +
                "guardian1_name, guardian1_relation, guardian1_mobile, guardian1_work_phone, guardian1_email, " +
                "guardian2_name, guardian2_relation, guardian2_mobile, guardian2_email, " +
                "notes, is_sick, is_missing_unexcused FROM students WHERE 1=1"
        );

        if (filterClassId != null) {
            sql.append(" AND class_id = ?");
        }

        List<Student> results = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            if (filterClassId != null) {
                ps.setLong(paramIdx++, filterClassId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && results.size() < limit) {
                    Student s = extractStudentFromResultSet(rs);

                    // Client-side filtering on decrypted data
                    if (searchTerm == null || searchTerm.isBlank() || matchesSearchTerm(s, searchTerm)) {
                        results.add(s);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Lädt alle Schüler (mit optionalem Limit)
     */
    public List<Student> findAll(int limit) throws SQLException {
        return searchStudents(null, null, limit);
    }

    private boolean matchesSearchTerm(Student s, String term) {
        if (term == null || term.isBlank()) return true;
        String lowerTerm = term.toLowerCase();

        return matches(s.getFirstName(), lowerTerm) ||
               matches(s.getLastName(), lowerTerm) ||
               matches(s.getStudentEmail(), lowerTerm) ||
               matches(s.getCity(), lowerTerm) ||
               matches(s.getNotes(), lowerTerm) ||
               matches(s.getGuardian1Name(), lowerTerm) ||
               matches(s.getGuardian2Name(), lowerTerm);
    }

    private boolean matches(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setFirstName(DataEncryptionService.decryptNullable(rs.getString("first_name")));
        s.setLastName(DataEncryptionService.decryptNullable(rs.getString("last_name")));
        long classId = rs.getLong("class_id");
        s.setClassId(rs.wasNull() ? null : classId);
        s.setStreet(DataEncryptionService.decryptNullable(rs.getString("street")));
        s.setPostalCode(DataEncryptionService.decryptNullable(rs.getString("postal_code")));
        s.setCity(DataEncryptionService.decryptNullable(rs.getString("city")));
        s.setCountry(DataEncryptionService.decryptNullable(rs.getString("country")));
        s.setStudentEmail(DataEncryptionService.decryptNullable(rs.getString("student_email")));
        s.setStudentMobile(DataEncryptionService.decryptNullable(rs.getString("student_mobile")));
        s.setGuardian1Name(DataEncryptionService.decryptNullable(rs.getString("guardian1_name")));
        s.setGuardian1Relation(DataEncryptionService.decryptNullable(rs.getString("guardian1_relation")));
        s.setGuardian1Mobile(DataEncryptionService.decryptNullable(rs.getString("guardian1_mobile")));
        s.setGuardian1WorkPhone(DataEncryptionService.decryptNullable(rs.getString("guardian1_work_phone")));
        s.setGuardian1Email(DataEncryptionService.decryptNullable(rs.getString("guardian1_email")));
        s.setGuardian2Name(DataEncryptionService.decryptNullable(rs.getString("guardian2_name")));
        s.setGuardian2Relation(DataEncryptionService.decryptNullable(rs.getString("guardian2_relation")));
        s.setGuardian2Mobile(DataEncryptionService.decryptNullable(rs.getString("guardian2_mobile")));
        s.setGuardian2Email(DataEncryptionService.decryptNullable(rs.getString("guardian2_email")));
        s.setNotes(DataEncryptionService.decryptNullable(rs.getString("notes")));
        s.setSick(rs.getBoolean("is_sick"));
        s.setMissingUnexcused(rs.getBoolean("is_missing_unexcused"));
        return s;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countSick() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE is_sick = 1";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countMissingUnexcused() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE is_missing_unexcused = 1";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<StudentItem> listAllItems() throws SQLException {
        String sql = "SELECT id, first_name, last_name FROM students";
        List<StudentItem> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String first = DataEncryptionService.decryptNullable(rs.getString("first_name"));
                String last = DataEncryptionService.decryptNullable(rs.getString("last_name"));
                list.add(new StudentItem(rs.getLong("id"), (last == null ? "" : last) + ", " + (first == null ? "" : first)));
            }
        }
        return list;
    }

    public static record StudentItem(long id, String displayName) {
        @Override public String toString() { return displayName; }
    }
}
