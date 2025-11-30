package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SchoolDatabaseOverviewController {

    @FXML private ListView<ClassItem> classList;

    @FXML private TableView<StudentRow> studentTable;
    @FXML private TableColumn<StudentRow, String> colStudentClass;
    @FXML private TableColumn<StudentRow, String> colStudentLast;
    @FXML private TableColumn<StudentRow, String> colStudentFirst;

    @FXML private TableView<TeacherRow> teacherTable;
    @FXML private TableColumn<TeacherRow, String> colTeacherLast;
    @FXML private TableColumn<TeacherRow, String> colTeacherFirst;
    @FXML private TableColumn<TeacherRow, String> colTeacherSubject;

    private final ObservableList<StudentRow> students = FXCollections.observableArrayList();
    private final ObservableList<TeacherRow> teachers = FXCollections.observableArrayList();

    private static final ClassItem ALL_CLASSES = new ClassItem(-1, "Alle Klassen", null);

    @FXML
    private void initialize() {
        // Students table binding
        if (colStudentClass != null) colStudentClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        if (colStudentLast != null) colStudentLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (colStudentFirst != null) colStudentFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        studentTable.setItems(students);
        studentTable.setPlaceholder(new Label("Keine Schüler vorhanden"));

        // Teachers table binding
        if (colTeacherLast != null) colTeacherLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (colTeacherFirst != null) colTeacherFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (colTeacherSubject != null) colTeacherSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        teacherTable.setItems(teachers);
        teacherTable.setPlaceholder(new Label("Keine Lehrer vorhanden"));

        // Load classes and data
        loadClasses();
        classList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> reloadStudents());
        // Default selection
        Platform.runLater(() -> {
            classList.getSelectionModel().select(0);
            reloadStudents();
        });

        reloadTeachers();
    }

    private void loadClasses() {
        try {
            ClassGroupRepository repo = new ClassGroupRepository();
            List<ClassGroupRepository.ClassItem> fromDb = repo.listAll();
            List<ClassItem> items = new ArrayList<>();
            items.add(ALL_CLASSES);
            for (var ci : fromDb) items.add(new ClassItem(ci.id, ci.name, ci.grade));
            classList.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Klassen", null, e);
            classList.setItems(FXCollections.observableArrayList(ALL_CLASSES));
        }
    }

    private void reloadStudents() {
        ClassItem selected = classList.getSelectionModel().getSelectedItem();
        long classId = (selected == null) ? -1 : selected.id;
        students.clear();
        String sqlAll = "SELECT s.first_name, s.last_name, c.name AS class_name " +
                "FROM students s LEFT JOIN class_groups c ON s.class_id = c.id " +
                "ORDER BY c.name IS NULL, c.name, s.last_name, s.first_name";
        String sqlByClass = "SELECT s.first_name, s.last_name, c.name AS class_name " +
                "FROM students s LEFT JOIN class_groups c ON s.class_id = c.id " +
                "WHERE s.class_id = ? ORDER BY s.last_name, s.first_name";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(classId <= 0 ? sqlAll : sqlByClass)) {
            if (classId > 0) ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new StudentRow(
                            nvl(rs.getString("first_name")),
                            nvl(rs.getString("last_name")),
                            nvl(rs.getString("class_name"))
                    ));
                }
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Schüler", null, e);
        }
    }

    private void reloadTeachers() {
        teachers.clear();
        String sql = "SELECT first_name, last_name, subject FROM teachers ORDER BY last_name, first_name";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                teachers.add(new TeacherRow(
                        nvl(rs.getString("first_name")),
                        nvl(rs.getString("last_name")),
                        nvl(rs.getString("subject"))
                ));
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Lehrer", null, e);
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    // Lightweight view models
    public static final class ClassItem {
        public final long id; public final String name; public final Integer grade;
        public ClassItem(long id, String name, Integer grade) { this.id = id; this.name = name; this.grade = grade; }
        @Override public String toString() { return name; }
    }

    public static final class StudentRow {
        private final String firstName; private final String lastName; private final String className;
        public StudentRow(String firstName, String lastName, String className) { this.firstName = firstName; this.lastName = lastName; this.className = className; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getClassName() { return className; }
    }

    public static final class TeacherRow {
        private final String firstName; private final String lastName; private final String subject;
        public TeacherRow(String firstName, String lastName, String subject) { this.firstName = firstName; this.lastName = lastName; this.subject = subject; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getSubject() { return subject; }
    }
}
