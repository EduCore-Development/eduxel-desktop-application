package dev.educore.eduxel.persistence.web;

import dev.educore.eduxel.domain.user.WebAccount;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.security.DataEncryptionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WebAccountRepository {

    public long create(WebAccount acc) throws SQLException {
        String sql = "INSERT INTO web_accounts(email, password_hash, type, reference_id) VALUES(?,?,?,?)";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, DataEncryptionService.encryptNullable(acc.getEmail()));
            ps.setString(2, acc.getPasswordHash()); // Password hash already hashed, or encrypted
            ps.setString(3, acc.getType());
            if (acc.getReferenceId() == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, acc.getReferenceId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kein Primärschlüssel generiert");
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM web_accounts";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<WebAccount> findAll() throws SQLException {
        String sql = "SELECT id, email, password_hash, type, reference_id, created_at FROM web_accounts";
        List<WebAccount> list = new ArrayList<>();
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WebAccount acc = new WebAccount();
                acc.setId(rs.getLong("id"));
                acc.setEmail(DataEncryptionService.decryptNullable(rs.getString("email")));
                acc.setPasswordHash(rs.getString("password_hash"));
                acc.setType(rs.getString("type"));
                long refId = rs.getLong("reference_id");
                acc.setReferenceId(rs.wasNull() ? null : refId);
                acc.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(acc);
            }
        }
        return list;
    }
}
