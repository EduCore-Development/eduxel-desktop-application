package dev.educore.eduxel.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaBootstrapper {
    private SchemaBootstrapper() {}

    public static void bootstrap() throws SQLException {
        try (Connection con = DataSourceProvider.getConnection(); Statement st = con.createStatement()) {
            st.addBatch("CREATE TABLE IF NOT EXISTS class_groups (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "grade INT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ");

            st.addBatch("CREATE TABLE IF NOT EXISTS students (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(100) NOT NULL, " +
                    "last_name VARCHAR(100) NOT NULL, " +
                    "class_id BIGINT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_students_class(class_id), " +
                    "INDEX idx_students_name(last_name, first_name), " +
                    "CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES class_groups(id) ON DELETE SET NULL) ");

            st.addBatch("CREATE TABLE IF NOT EXISTS teachers (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(100) NOT NULL, " +
                    "last_name VARCHAR(100) NOT NULL, " +
                    "subject VARCHAR(120) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ");

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

            st.addBatch("CREATE TABLE IF NOT EXISTS device_assignments (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "device_id BIGINT NOT NULL, " +
                    "assignee_type VARCHAR(16) NOT NULL, " +
                    "assignee_id BIGINT NOT NULL, " +
                    "assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "returned_at TIMESTAMP NULL, " +
                    "INDEX idx_assignments_device(device_id), " +
                    "CONSTRAINT fk_assignment_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE) ");

            st.addBatch("CREATE TABLE IF NOT EXISTS activities (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "area VARCHAR(40) NOT NULL, " +
                    "action VARCHAR(255) NOT NULL, " +
                    "user VARCHAR(120) NULL, " +
                    "INDEX idx_activity_time(time)) ");

            st.executeBatch();
        }
    }
}
