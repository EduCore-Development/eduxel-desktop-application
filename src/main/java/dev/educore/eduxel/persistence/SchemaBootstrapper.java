package dev.educore.eduxel.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaBootstrapper {
    private SchemaBootstrapper() {}

    public static void bootstrap() throws SQLException {
        try (Connection con = DataSourceProvider.getConnection(); Statement st = con.createStatement()) {
            // Basistabellen anlegen (idempotent)
            // 1. Lehrer (mit E-Mail und Telefon)
            // Größere VARCHAR-Längen, um Base64-ciphertexts bequem zu speichern
            st.addBatch("CREATE TABLE IF NOT EXISTS teachers (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(512) NOT NULL, " +
                    "last_name VARCHAR(512) NOT NULL, " +
                    "subject VARCHAR(120) NULL, " +
                    "email VARCHAR(512) NULL, " +
                    "phone VARCHAR(128) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ");

            // 2. Klassen (FK auf teachers)
            st.addBatch("CREATE TABLE IF NOT EXISTS class_groups (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "grade INT NULL, " +
                    "school_type VARCHAR(120) NULL, " +
                    "room VARCHAR(40) NULL, " +
                    "teacher_id BIGINT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL) ");

            // 3. Schüler (FK auf class_groups)
            st.addBatch("CREATE TABLE IF NOT EXISTS students (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(512) NOT NULL, " +
                    "last_name VARCHAR(512) NOT NULL, " +
                    "class_id BIGINT NULL, " +
                    "street VARCHAR(512) NULL, " +
                    "postal_code VARCHAR(128) NULL, " +
                    "city VARCHAR(512) NULL, " +
                    "country VARCHAR(256) NULL, " +
                    "student_email VARCHAR(512) NULL, " +
                    "student_mobile VARCHAR(128) NULL, " +
                    "guardian1_name VARCHAR(512) NULL, " +
                    "guardian1_relation VARCHAR(256) NULL, " +
                    "guardian1_mobile VARCHAR(128) NULL, " +
                    "guardian1_work_phone VARCHAR(128) NULL, " +
                    "guardian1_email VARCHAR(512) NULL, " +
                    "guardian2_name VARCHAR(512) NULL, " +
                    "guardian2_relation VARCHAR(256) NULL, " +
                    "guardian2_mobile VARCHAR(128) NULL, " +
                    "guardian2_email VARCHAR(512) NULL, " +
                    "notes MEDIUMTEXT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_students_class(class_id), " +
                    "INDEX idx_students_name(last_name, first_name), " +
                    "CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES class_groups(id) ON DELETE SET NULL) ");

            // 4. Geräte (FKs auf students/teachers)
            st.addBatch("CREATE TABLE IF NOT EXISTS devices (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "asset_tag VARCHAR(100) NOT NULL UNIQUE, " +
                    "type VARCHAR(40) NULL, " +
                    "status VARCHAR(40) NULL, " +
                    "owner_student_id BIGINT NULL, " +
                    "owner_teacher_id BIGINT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_devices_asset(asset_tag), " +
                    "CONSTRAINT fk_dev_owner_student FOREIGN KEY (owner_student_id) REFERENCES students(id) ON DELETE SET NULL, " +
                    "CONSTRAINT fk_dev_owner_teacher FOREIGN KEY (owner_teacher_id) REFERENCES teachers(id) ON DELETE SET NULL) ");

            // 5. Geräte-Zuordnungen
            st.addBatch("CREATE TABLE IF NOT EXISTS device_assignments (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "device_id BIGINT NOT NULL, " +
                    "assignee_type VARCHAR(16) NOT NULL, " +
                    "assignee_id BIGINT NOT NULL, " +
                    "assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "returned_at TIMESTAMP NULL, " +
                    "INDEX idx_assignments_device(device_id), " +
                    "CONSTRAINT fk_assignment_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE) ");

            // 6. Aktivitäten
            st.addBatch("CREATE TABLE IF NOT EXISTS activities (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "area VARCHAR(40) NOT NULL, " +
                    "action VARCHAR(255) NOT NULL, " +
                    "user VARCHAR(120) NULL, " +
                    "INDEX idx_activity_time(time)) ");

            st.executeBatch();

            // Schema-Migrationen (idempotente ALTER TABLEs)
            applyMigrations(con);
        }
    }

    private static void applyMigrations(Connection con) {
        try (Statement st = con.createStatement()) {
            // Lehrer: fehlende Spalten email/phone nachziehen, falls alte Tabellenstruktur vorhanden ist.
            // MySQL kennt "IF NOT EXISTS" für ADD COLUMN erst ab neueren Versionen; hier best-effort, Fehler werden ignoriert.
            try {
                st.executeUpdate("ALTER TABLE teachers ADD COLUMN email VARCHAR(200) NULL");
            } catch (SQLException ignored) { }
            try {
                st.executeUpdate("ALTER TABLE teachers ADD COLUMN phone VARCHAR(40) NULL");
            } catch (SQLException ignored) { }

            // Spalten vergrößern, um verschlüsselte (Base64) Daten zu fassen (idempotent; Fehler ignorieren)
            try { st.executeUpdate("ALTER TABLE teachers MODIFY COLUMN first_name VARCHAR(512) NOT NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE teachers MODIFY COLUMN last_name VARCHAR(512) NOT NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE teachers MODIFY COLUMN email VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE teachers MODIFY COLUMN phone VARCHAR(128) NULL"); } catch (SQLException ignored) {}

            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN first_name VARCHAR(512) NOT NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN last_name VARCHAR(512) NOT NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN street VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN postal_code VARCHAR(128) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN city VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN country VARCHAR(256) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN student_email VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN student_mobile VARCHAR(128) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian1_name VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian1_relation VARCHAR(256) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian1_mobile VARCHAR(128) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian1_work_phone VARCHAR(128) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian1_email VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian2_name VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian2_relation VARCHAR(256) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian2_mobile VARCHAR(128) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN guardian2_email VARCHAR(512) NULL"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE students MODIFY COLUMN notes MEDIUMTEXT NULL"); } catch (SQLException ignored) {}

            // Weitere Migrationen für andere Tabellen könnten hier folgen (z. B. neue Schüler-Felder, Indizes, etc.)
        } catch (SQLException ignored) {
            // Wir wollen nicht, dass das UI durch Migrationsfehler unbenutzbar wird.
        }
    }
}
