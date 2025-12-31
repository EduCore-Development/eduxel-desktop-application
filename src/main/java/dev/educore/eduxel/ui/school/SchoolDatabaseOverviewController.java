package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.persistence.school.TeacherRepository;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.security.DataEncryptionService;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @FXML private Button btnAddStudent;


    @FXML private Button btnViewStudent;
    @FXML private Button btnViewTeacher;
    @FXML private Button btnViewClass;

    private final ObservableList<StudentRow> students = FXCollections.observableArrayList();
    private final ObservableList<TeacherRow> teachers = FXCollections.observableArrayList();

    private static final ClassItem ALL_CLASSES = new ClassItem(-1, "Alle Klassen", null);

    private final SchoolService schoolService = new SchoolService();

    @FXML
    private void initialize() {
        // Students table binding
        if (colStudentClass != null) colStudentClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        if (colStudentLast != null) colStudentLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (colStudentFirst != null) colStudentFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        studentTable.setItems(students);
        studentTable.setPlaceholder(new Label("Keine Schüler vorhanden"));

        // Kontextmenü und Doppelklick für Schüler
        installStudentContextMenu();

        // Kontextmenü für Klassenliste
        installClassContextMenu();

        // Kontextmenü für Lehrertabelle
        installTeacherContextMenu();

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

        if (btnAddStudent != null) {
            btnAddStudent.setOnAction(e -> onAddStudent());
        }
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
        String sqlAll = "SELECT s.id, s.first_name, s.last_name, c.name AS class_name " +
                "FROM students s LEFT JOIN class_groups c ON s.class_id = c.id";
        String sqlByClass = "SELECT s.id, s.first_name, s.last_name, c.name AS class_name " +
                "FROM students s LEFT JOIN class_groups c ON s.class_id = c.id " +
                "WHERE s.class_id = ?";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(classId <= 0 ? sqlAll : sqlByClass)) {
            if (classId > 0) ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String first = DataEncryptionService.decryptNullable(rs.getString("first_name"));
                    String last = DataEncryptionService.decryptNullable(rs.getString("last_name"));
                    students.add(new StudentRow(
                            rs.getLong("id"),
                            nvl(first),
                            nvl(last),
                            nvl(rs.getString("class_name"))
                    ));
                }
            }
            // In-memory sort: by className (nulls last), then last, then first (case-insensitive)
            students.sort((a, b) -> {
                String ca = a.getClassName();
                String cb = b.getClassName();
                int cmpClass = (ca == null || ca.isBlank()) && (cb == null || cb.isBlank()) ? 0
                        : (ca == null || ca.isBlank()) ? 1
                        : (cb == null || cb.isBlank()) ? -1
                        : ca.compareToIgnoreCase(cb);
                if (cmpClass != 0) return cmpClass;
                int cmpLast = nvl(a.getLastName()).compareToIgnoreCase(nvl(b.getLastName()));
                if (cmpLast != 0) return cmpLast;
                return nvl(a.getFirstName()).compareToIgnoreCase(nvl(b.getFirstName()));
            });
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Schüler", null, e);
        }
    }

    private void reloadTeachers() {
        teachers.clear();
        String sql = "SELECT id, first_name, last_name, subject FROM teachers";
        try (Connection con = DataSourceProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String first = DataEncryptionService.decryptNullable(rs.getString("first_name"));
                String last = DataEncryptionService.decryptNullable(rs.getString("last_name"));
                teachers.add(new TeacherRow(
                        rs.getLong("id"),
                        nvl(first),
                        nvl(last),
                        nvl(rs.getString("subject"))
                ));
            }
            // In-memory sort: by last, then first
            teachers.sort((a, b) -> {
                int cmpLast = nvl(a.getLastName()).compareToIgnoreCase(nvl(b.getLastName()));
                if (cmpLast != 0) return cmpLast;
                return nvl(a.getFirstName()).compareToIgnoreCase(nvl(b.getFirstName()));
            });
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Lehrer", null, e);
        }
    }

    private void installStudentContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("Schülerdaten öffnen …");
        MenuItem changeClassItem = new MenuItem("Klasse zuweisen/ändern …");
        MenuItem deleteItem = new MenuItem("Schüler löschen …");

        editItem.setOnAction(e -> onEditSelectedStudent());
        changeClassItem.setOnAction(e -> onChangeClassForSelectedStudent());
        deleteItem.setOnAction(e -> onDeleteSelectedStudent());

        menu.getItems().setAll(editItem, changeClassItem, new SeparatorMenuItem(), deleteItem);

        studentTable.setRowFactory(tv -> {
            TableRow<StudentRow> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    onEditStudent(row.getItem());
                }
            });

            row.setOnContextMenuRequested(evt -> {
                if (!row.isEmpty()) {
                    studentTable.getSelectionModel().select(row.getItem());
                    menu.show(row, evt.getScreenX(), evt.getScreenY());
                } else {
                    menu.hide();
                }
            });

            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(menu);
                }
            });

            return row;
        });
    }

    private void installClassContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem editClassItem = new MenuItem("Klasse bearbeiten …");
        MenuItem deleteClassItem = new MenuItem("Klasse löschen …");

        editClassItem.setOnAction(e -> onEditSelectedClass());
        deleteClassItem.setOnAction(e -> onDeleteSelectedClass());

        menu.getItems().setAll(editClassItem, new SeparatorMenuItem(), deleteClassItem);

        classList.setCellFactory(lv -> {
            ListCell<ClassItem> cell = new ListCell<>() {
                @Override
                protected void updateItem(ClassItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.name);
                }
            };

            cell.setOnMouseClicked(evt -> {
                if (!cell.isEmpty() && evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                    onEditSelectedClass();
                }
            });

            cell.setOnContextMenuRequested(evt -> {
                if (!cell.isEmpty() && cell.getItem() != ALL_CLASSES) {
                    classList.getSelectionModel().select(cell.getItem());
                    menu.show(cell, evt.getScreenX(), evt.getScreenY());
                } else {
                    menu.hide();
                }
            });

            return cell;
        });
    }

    private void installTeacherContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem viewItem = new MenuItem("Lehrerdaten anzeigen …");
        MenuItem deleteItem = new MenuItem("Lehrer löschen …");

        viewItem.setOnAction(e -> onViewSelectedTeacher());
        // deleteItem.setOnAction(e -> onDeleteSelectedTeacher()); // optional später

        menu.getItems().setAll(viewItem, new SeparatorMenuItem() /*, deleteItem*/);

        teacherTable.setRowFactory(tv -> {
            TableRow<TeacherRow> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    onViewSelectedTeacher();
                }
            });

            row.setOnContextMenuRequested(evt -> {
                if (!row.isEmpty()) {
                    teacherTable.getSelectionModel().select(row.getItem());
                    menu.show(row, evt.getScreenX(), evt.getScreenY());
                } else {
                    menu.hide();
                }
            });

            return row;
        });
    }

    private void onEditSelectedStudent() {
        StudentRow row = studentTable.getSelectionModel().getSelectedItem();
        if (row != null) onEditStudent(row);
    }

    private void onChangeClassForSelectedStudent() {
        // aktuell: gleicher Dialog, Fokus liegt dann auf Klassenfeld
        onEditSelectedStudent();
    }

    private void onDeleteSelectedStudent() {
        StudentRow row = studentTable.getSelectionModel().getSelectedItem();
        if (row == null) return;

        if (FxUtils.showConfirmation("Schüler löschen", "Schüler löschen", "Soll der Schüler '" + row.getFirstName() + " " + row.getLastName() + "' wirklich gelöscht werden?")) {
            try {
                schoolService.deleteStudent(row.getId());
                reloadStudents();
            } catch (Exception ex) {
                FxUtils.showError("Fehler beim Löschen des Schülers", null, ex);
            }
        }
    }

    private void onEditStudent(StudentRow row) {
        try {
            var studentOpt = schoolService.loadStudent(row.getId());
            if (studentOpt.isEmpty()) {
                FxUtils.showError("Schüler nicht gefunden", null, null);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            DialogPane pane = loader.load();
            StudentDetailController controller = loader.getController();
            controller.initForEdit(studentOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Schüler bearbeiten");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                var updated = controller.buildResult();
                schoolService.updateStudent(updated);
                reloadStudents();
            }
        } catch (IOException ex) {
            FxUtils.showError("Fehler beim Öffnen des Schülerdialogs", null, ex);
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Speichern des Schülers", null, ex);
        }
    }

    private void onAddStudent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            DialogPane pane = loader.load();
            StudentDetailController controller = loader.getController();
            controller.initForCreate();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neuen Schüler anlegen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Student created = controller.buildResult();
                // Voreingestellte Klasse aus der linken Liste übernehmen, falls vorhanden
                ClassItem selClass = classList.getSelectionModel().getSelectedItem();
                if (selClass != null && selClass.id > 0) {
                    created.setClassId(selClass.id);
                }
                schoolService.createStudent(created);
                reloadStudents();
            }
        } catch (IOException ex) {
            FxUtils.showError("Fehler beim Öffnen des Schülerdialogs", null, ex);
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Anlegen des Schülers", null, ex);
        }
    }

    private void onEditSelectedClass() {
        ClassItem item = classList.getSelectionModel().getSelectedItem();
        if (item == null || item == ALL_CLASSES) return;

        try {
            Optional<ClassGroup> groupOpt = schoolService.loadClass(item.id);
            if (groupOpt.isEmpty()) {
                FxUtils.showError("Klasse nicht gefunden", null, null);
                return;
            }
            ClassGroup group = groupOpt.get();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            DialogPane pane = loader.load();
            ClassDetailController controller = loader.getController();
            controller.initForEdit(group);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Klasse bearbeiten");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;

                group.setName(controller.getName());
                group.setGrade(controller.getGrade());
                group.setSchoolType(controller.getSchoolType());
                group.setRoom(controller.getRoom());
                var teacherItem = controller.getSelectedTeacher();
                group.setTeacherId(teacherItem != null ? teacherItem.id : null);

                schoolService.updateClass(group);
                loadClasses();
                reloadStudents();
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Bearbeiten der Klasse", null, e);
        }
    }

    private void onDeleteSelectedClass() {
        ClassItem item = classList.getSelectionModel().getSelectedItem();
        if (item == null || item == ALL_CLASSES) return;

        if (FxUtils.showConfirmation("Klasse löschen", "Klasse löschen", "Soll die Klasse '" + item.name + "' wirklich gelöscht werden? Alle zugeordneten Schüler verlieren ihre Klassen-Zuordnung.")) {
            try {
                schoolService.deleteClass(item.id);
                loadClasses();
                reloadStudents();
            } catch (Exception e) {
                FxUtils.showError("Fehler beim Löschen der Klasse", null, e);
            }
        }
    }

    @FXML
    private void onViewSelectedStudent() {
        StudentRow row = studentTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            FxUtils.showInfo("Keine Auswahl", "Bitte wähle zuerst einen Schüler aus.");
            return;
        }
        try {
            var studentOpt = schoolService.loadStudent(row.getId());
            if (studentOpt.isEmpty()) {
                FxUtils.showError("Schüler nicht gefunden", null, null);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            DialogPane pane = loader.load();
            StudentDetailController controller = loader.getController();
            controller.initForEdit(studentOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Schüler anzeigen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.showAndWait();
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Öffnen des Schülerdialogs", null, ex);
        }
    }

    @FXML
    private void onViewSelectedClass() {
        ClassItem item = classList.getSelectionModel().getSelectedItem();
        if (item == null || item == ALL_CLASSES) {
            FxUtils.showInfo("Keine Auswahl", "Bitte wähle zuerst eine konkrete Klasse aus.");
            return;
        }

        try {
            Optional<ClassGroup> groupOpt = schoolService.loadClass(item.id);
            if (groupOpt.isEmpty()) {
                FxUtils.showError("Klasse nicht gefunden", null, null);
                return;
            }
            ClassGroup group = groupOpt.get();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            DialogPane pane = loader.load();
            ClassDetailController controller = loader.getController();
            controller.initForEdit(group);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Klasse anzeigen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.showAndWait();
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Öffnen des Klassendialogs", null, e);
        }
    }

    @FXML
    private void onViewSelectedTeacher() {
        TeacherRow row = teacherTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            FxUtils.showInfo("Keine Auswahl", "Bitte wähle zuerst einen Lehrer aus.");
            return;
        }
        try {
            Optional<Teacher> teacherOpt = schoolService.loadTeacher(row.getId());
            if (teacherOpt.isEmpty()) {
                FxUtils.showError("Lehrer nicht gefunden", null, null);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/teacher-detail.fxml"));
            DialogPane pane = loader.load();
            TeacherDetailController controller = loader.getController();
            controller.initForEdit(teacherOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Lehrer anzeigen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;
                Teacher updated = controller.buildResult();
                schoolService.updateTeacher(updated);
                reloadTeachers();
            }
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Öffnen des Lehrerdialogs", null, ex);
        }
    }

    @FXML
    private void onQuickCreateClass() {
        // Delegiert an den bestehenden Quick-Create-Flow im MainWindow oder nutzt lokal den Klassendialog.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            DialogPane pane = loader.load();
            ClassDetailController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neue Klasse anlegen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;

                ClassGroup group = new ClassGroup();
                group.setName(controller.getName());
                group.setGrade(controller.getGrade());
                group.setSchoolType(controller.getSchoolType());
                group.setRoom(controller.getRoom());
                var teacherItem = controller.getSelectedTeacher();
                if (teacherItem != null) group.setTeacherId(teacherItem.id);

                schoolService.createClass(group);
                loadClasses();
                reloadStudents();
            }
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Anlegen der Klasse", null, ex);
        }
    }

    @FXML
    private void onQuickCreateTeacher() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/teacher-detail.fxml"));
            DialogPane pane = loader.load();
            TeacherDetailController controller = loader.getController();
            controller.initForCreate();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neuen Lehrer anlegen");
            dialog.initModality(Modality.APPLICATION_MODAL);
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;

                // Solange wir noch keine Teacher-Domainklasse haben, nutzen wir den einfachen Service-Call
                schoolService.createTeacher(
                        controller.getFirstName(),
                        controller.getLastName(),
                        controller.getSubject()
                );
                // Email/Telefon werden später mit einer erweiterten Lehrer-Persistenz ergänzt
                reloadTeachers();
            }
        } catch (Exception ex) {
            FxUtils.showError("Fehler beim Anlegen des Lehrers", null, ex);
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
        private final long id; private final String firstName; private final String lastName; private final String className;
        public StudentRow(long id, String firstName, String lastName, String className) { this.id = id; this.firstName = firstName; this.lastName = lastName; this.className = className; }
        public long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getClassName() { return className; }
    }

    public static final class TeacherRow {
        private final long id;
        private final String firstName;
        private final String lastName;
        private final String subject;
        public TeacherRow(long id, String firstName, String lastName, String subject) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.subject = subject;
        }
        public long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getSubject() { return subject; }
    }
}
