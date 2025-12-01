package dev.educore.eduxel.persistence.school;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.persistence.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassGroupRepository {

    public Optional<Long> findIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM class_groups WHERE name = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong(1));
                return Optional.empty();
            }
        }
    }

    public long create(String name, Integer grade) throws SQLException {
        String sql = "INSERT INTO class_groups(name, grade) VALUES(?, ?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            if (grade == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, grade);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public long createFull(ClassGroup group) throws SQLException {
        String sql = "INSERT INTO class_groups(name, grade, school_type, room, teacher_id) VALUES(?,?,?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            if (group.getGrade() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, group.getGrade());
            ps.setString(3, group.getSchoolType());
            ps.setString(4, group.getRoom());
            if (group.getTeacherId() == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, group.getTeacherId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public List<ClassItem> listAll() throws SQLException {
        String sql = "SELECT id, name, grade FROM class_groups ORDER BY name";
        List<ClassItem> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ClassItem(rs.getLong("id"), rs.getString("name"), (Integer) rs.getObject("grade")));
            }
        }
        return list;
    }

    public void update(dev.educore.eduxel.domain.school.ClassGroup group) throws SQLException {
        String sql = "UPDATE class_groups SET name = ?, grade = ?, school_type = ?, room = ?, teacher_id = ? WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, group.getName());
            if (group.getGrade() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, group.getGrade());
            ps.setString(3, group.getSchoolType());
            ps.setString(4, group.getRoom());
            if (group.getTeacherId() == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, group.getTeacherId());
            ps.setLong(6, group.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM class_groups WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public ClassGroup findById(long id) throws SQLException {
        String sql = "SELECT id, name, grade, school_type, room, teacher_id FROM class_groups WHERE id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ClassGroup g = new ClassGroup();
                g.setId(rs.getLong("id"));
                g.setName(rs.getString("name"));
                g.setGrade((Integer) rs.getObject("grade"));
                g.setSchoolType(rs.getString("school_type"));
                g.setRoom(rs.getString("room"));
                Object tid = rs.getObject("teacher_id");
                g.setTeacherId(tid == null ? null : ((Number) tid).longValue());
                return g;
            }
        }
    }

    public static final class ClassItem {
        public final long id;
        public final String name;
        public final Integer grade;

        public ClassItem(long id, String name, Integer grade) {
            this.id = id;
            this.name = name;
            this.grade = grade;
        }

        @Override public String toString() { return name; }
    }
}
