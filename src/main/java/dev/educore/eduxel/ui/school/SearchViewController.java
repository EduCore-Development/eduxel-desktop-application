package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.service.SearchService;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller für die globale Such-View
 */
public class SearchViewController {

    @FXML private TextField searchField;
    @FXML private Button btnClear;
    @FXML private ComboBox<String> filterClassCombo;
    @FXML private ComboBox<Integer> filterGradeCombo;
    @FXML private ComboBox<String> filterSubjectCombo;
    @FXML private Label resultCountLabel;

    @FXML private TabPane resultsTabPane;

    // Schüler Tab
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colStudentClass;
    @FXML private TableColumn<Student, String> colStudentEmail;
    @FXML private TableColumn<Student, String> colStudentCity;
    @FXML private TableColumn<Student, String> colStudentNotes;
    @FXML private Label studentCountLabel;

    // Lehrer Tab
    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> colTeacherName;
    @FXML private TableColumn<Teacher, String> colTeacherSubject;
    @FXML private TableColumn<Teacher, String> colTeacherEmail;
    @FXML private TableColumn<Teacher, String> colTeacherPhone;
    @FXML private Label teacherCountLabel;

    // Klassen Tab
    @FXML private TableView<ClassGroup> classTable;
    @FXML private TableColumn<ClassGroup, String> colClassName;
    @FXML private TableColumn<ClassGroup, Integer> colClassGrade;
    @FXML private TableColumn<ClassGroup, String> colClassType;
    @FXML private TableColumn<ClassGroup, String> colClassRoom;
    @FXML private Label classCountLabel;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Teacher> teachers = FXCollections.observableArrayList();
    private final ObservableList<ClassGroup> classes = FXCollections.observableArrayList();

    private final SearchService searchService = new SearchService();
    private final SchoolService schoolService = new SchoolService();
    private final ClassGroupRepository classRepo = new ClassGroupRepository();

    private PauseTransition searchDebounce;

    @FXML
    private void initialize() {
        setupTables();
        setupFilters();
        setupSearchDebounce();
        setupDoubleClickHandlers();
        setupContextMenus();

        // Initial load
        performSearch();
    }

    private void setupTables() {
        // Schüler Tabelle
        if (colStudentName != null) {
            colStudentName.setCellValueFactory(cellData -> {
                Student s = cellData.getValue();
                String name = (s.getLastName() != null ? s.getLastName() : "") + ", " +
                             (s.getFirstName() != null ? s.getFirstName() : "");
                return new SimpleStringProperty(name);
            });
        }
        if (colStudentClass != null) {
            colStudentClass.setCellValueFactory(cellData -> {
                Student s = cellData.getValue();
                return new SimpleStringProperty(getClassNameById(s.getClassId()));
            });
        }
        if (colStudentEmail != null) {
            colStudentEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentEmail()));
        }
        if (colStudentCity != null) {
            colStudentCity.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCity()));
        }
        if (colStudentNotes != null) {
            colStudentNotes.setCellValueFactory(cellData -> {
                String notes = cellData.getValue().getNotes();
                String preview = notes != null && notes.length() > 50
                    ? notes.substring(0, 50) + "..."
                    : notes;
                return new SimpleStringProperty(preview);
            });
        }
        studentTable.setItems(students);
        studentTable.setPlaceholder(new Label("Keine Schüler gefunden"));

        // Lehrer Tabelle
        if (colTeacherName != null) {
            colTeacherName.setCellValueFactory(cellData -> {
                Teacher t = cellData.getValue();
                String name = (t.getLastName() != null ? t.getLastName() : "") + ", " +
                             (t.getFirstName() != null ? t.getFirstName() : "");
                return new SimpleStringProperty(name);
            });
        }
        if (colTeacherSubject != null) {
            colTeacherSubject.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject()));
        }
        if (colTeacherEmail != null) {
            colTeacherEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));
        }
        if (colTeacherPhone != null) {
            colTeacherPhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhone()));
        }
        teacherTable.setItems(teachers);
        teacherTable.setPlaceholder(new Label("Keine Lehrer gefunden"));

        // Klassen Tabelle
        if (colClassName != null) {
            colClassName.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (colClassGrade != null) {
            colClassGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        }
        if (colClassType != null) {
            colClassType.setCellValueFactory(new PropertyValueFactory<>("schoolType"));
        }
        if (colClassRoom != null) {
            colClassRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
        }
        classTable.setItems(classes);
        classTable.setPlaceholder(new Label("Keine Klassen gefunden"));
    }

    private void setupFilters() {
        // Klassen-Filter laden
        try {
            List<ClassGroupRepository.ClassItem> classItems = classRepo.listAll();
            ObservableList<String> classNames = FXCollections.observableArrayList("Alle Klassen");
            classItems.forEach(item -> classNames.add(item.name));
            filterClassCombo.setItems(classNames);
            filterClassCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Jahrgangs-Filter
        ObservableList<Integer> grades = FXCollections.observableArrayList(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
        );
        filterGradeCombo.setItems(grades);

        // Fächer-Filter (Beispiel)
        ObservableList<String> subjects = FXCollections.observableArrayList(
            "Alle Fächer", "Mathematik", "Deutsch", "Englisch", "Französisch",
            "Biologie", "Chemie", "Physik", "Geschichte", "Geographie",
            "Sport", "Musik", "Kunst", "Informatik"
        );
        filterSubjectCombo.setItems(subjects);
        filterSubjectCombo.getSelectionModel().selectFirst();
    }

    private void setupSearchDebounce() {
        // Debounce: Suche startet erst 300ms nach letzter Eingabe
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(event -> performSearch());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                searchDebounce.playFromStart();
            });
        }
    }

    private void setupDoubleClickHandlers() {
        // Doppelklick auf Schüler
        studentTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onViewSelectedStudent();
            }
        });

        // Doppelklick auf Lehrer
        teacherTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onViewSelectedTeacher();
            }
        });

        // Doppelklick auf Klasse
        classTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onViewSelectedClass();
            }
        });
    }

    private void setupContextMenus() {
        // Kontextmenü für Schüler
        ContextMenu studentContextMenu = new ContextMenu();

        MenuItem viewStudent = new MenuItem("👁 Anzeigen");
        viewStudent.setOnAction(e -> onViewSelectedStudent());

        MenuItem editStudent = new MenuItem("✏️ Bearbeiten");
        editStudent.setOnAction(e -> onEditSelectedStudent());

        MenuItem deleteStudent = new MenuItem("🗑 Löschen");
        deleteStudent.setOnAction(e -> onDeleteSelectedStudent());

        MenuItem exportStudent = new MenuItem("📄 Exportieren");
        exportStudent.setOnAction(e -> onExportStudent());

        studentContextMenu.getItems().addAll(viewStudent, editStudent, new SeparatorMenuItem(), deleteStudent, new SeparatorMenuItem(), exportStudent);
        studentTable.setContextMenu(studentContextMenu);

        // Kontextmenü für Lehrer
        ContextMenu teacherContextMenu = new ContextMenu();

        MenuItem viewTeacher = new MenuItem("👁 Anzeigen");
        viewTeacher.setOnAction(e -> onViewSelectedTeacher());

        MenuItem editTeacher = new MenuItem("✏️ Bearbeiten");
        editTeacher.setOnAction(e -> onEditSelectedTeacher());

        MenuItem deleteTeacher = new MenuItem("🗑 Löschen");
        deleteTeacher.setOnAction(e -> onDeleteSelectedTeacher());

        MenuItem exportTeacher = new MenuItem("📄 Exportieren");
        exportTeacher.setOnAction(e -> onExportTeacher());

        teacherContextMenu.getItems().addAll(viewTeacher, editTeacher, new SeparatorMenuItem(), deleteTeacher, new SeparatorMenuItem(), exportTeacher);
        teacherTable.setContextMenu(teacherContextMenu);

        // Kontextmenü für Klassen
        ContextMenu classContextMenu = new ContextMenu();

        MenuItem viewClass = new MenuItem("👁 Anzeigen");
        viewClass.setOnAction(e -> onViewSelectedClass());

        MenuItem editClass = new MenuItem("✏️ Bearbeiten");
        editClass.setOnAction(e -> onEditSelectedClass());

        MenuItem deleteClass = new MenuItem("🗑 Löschen");
        deleteClass.setOnAction(e -> onDeleteSelectedClass());

        MenuItem showClassStudents = new MenuItem("👥 Schüler dieser Klasse");
        showClassStudents.setOnAction(e -> onShowClassStudents());

        classContextMenu.getItems().addAll(viewClass, editClass, new SeparatorMenuItem(), deleteClass, new SeparatorMenuItem(), showClassStudents);
        classTable.setContextMenu(classContextMenu);
    }

    @FXML
    private void onClear() {
        if (searchField != null) searchField.clear();
        if (filterClassCombo != null) filterClassCombo.getSelectionModel().selectFirst();
        if (filterGradeCombo != null) filterGradeCombo.getSelectionModel().clearSelection();
        if (filterSubjectCombo != null) filterSubjectCombo.getSelectionModel().selectFirst();
        performSearch();
    }

    @FXML
    private void onFilterChanged() {
        performSearch();
    }

    private void performSearch() {
        String searchTerm = searchField != null ? searchField.getText() : "";
        Long classFilter = getSelectedClassId();
        Integer gradeFilter = filterGradeCombo != null ? filterGradeCombo.getValue() : null;
        String subjectFilter = getSelectedSubject();

        // Asynchrone Suche mit JavaFX Task
        Task<Void> searchTask = new Task<>() {
            private List<Student> foundStudents;
            private List<Teacher> foundTeachers;
            private List<ClassGroup> foundClasses;

            @Override
            protected Void call() throws Exception {
                foundStudents = searchService.searchStudents(searchTerm, classFilter);
                foundTeachers = searchService.searchTeachers(searchTerm, subjectFilter);
                foundClasses = searchService.searchClasses(searchTerm, gradeFilter);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    students.setAll(foundStudents);
                    teachers.setAll(foundTeachers);
                    classes.setAll(foundClasses);
                    updateResultCounts();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    FxUtils.showError("Suchfehler", "Die Suche konnte nicht durchgeführt werden.",
                                     getException());
                });
            }
        };

        new Thread(searchTask).start();
    }

    private void updateResultCounts() {
        int total = students.size() + teachers.size() + classes.size();

        if (resultCountLabel != null) {
            resultCountLabel.setText(total + " Ergebnis" + (total != 1 ? "se" : ""));
        }
        if (studentCountLabel != null) {
            studentCountLabel.setText(students.size() + " Schüler gefunden");
        }
        if (teacherCountLabel != null) {
            teacherCountLabel.setText(teachers.size() + " Lehrer gefunden");
        }
        if (classCountLabel != null) {
            classCountLabel.setText(classes.size() + " Klassen gefunden");
        }
    }

    @FXML
    private void onViewSelectedStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Schüler aus.");
            return;
        }
        openStudentDetail(selected.getId());
    }

    @FXML
    private void onViewSelectedTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Lehrer aus.");
            return;
        }
        openTeacherDetail(selected.getId());
    }

    @FXML
    private void onViewSelectedClass() {
        ClassGroup selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie eine Klasse aus.");
            return;
        }
        openClassDetail(selected.getId());
    }

    private void openStudentDetail(Long studentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            Scene scene = new Scene(loader.load());

            StudentDetailController controller = loader.getController();
            if (controller != null) {
                controller.setStudentId(studentId);
            }

            Stage stage = new Stage();
            stage.setTitle("Schülerdetails");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh nach Schließen
            performSearch();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Schülerdetails konnten nicht geöffnet werden.", e);
        }
    }

    private void openTeacherDetail(Long teacherId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/teacher-detail.fxml"));
            Scene scene = new Scene(loader.load());

            TeacherDetailController controller = loader.getController();
            if (controller != null) {
                controller.setTeacherId(teacherId);
            }

            Stage stage = new Stage();
            stage.setTitle("Lehrerdetails");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            performSearch();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Lehrerdetails konnten nicht geöffnet werden.", e);
        }
    }

    private void openClassDetail(Long classId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            Scene scene = new Scene(loader.load());

            ClassDetailController controller = loader.getController();
            if (controller != null) {
                controller.setClassId(classId);
            }

            Stage stage = new Stage();
            stage.setTitle("Klassendetails");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            performSearch();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Klassendetails konnten nicht geöffnet werden.", e);
        }
    }

    private Long getSelectedClassId() {
        if (filterClassCombo == null || filterClassCombo.getValue() == null) return null;
        String selected = filterClassCombo.getValue();
        if ("Alle Klassen".equals(selected)) return null;

        try {
            List<ClassGroupRepository.ClassItem> items = classRepo.listAll();
            for (ClassGroupRepository.ClassItem item : items) {
                if (item.name.equals(selected)) {
                    return item.id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSelectedSubject() {
        if (filterSubjectCombo == null || filterSubjectCombo.getValue() == null) return null;
        String selected = filterSubjectCombo.getValue();
        return "Alle Fächer".equals(selected) ? null : selected;
    }

    private String getClassNameById(Long classId) {
        if (classId == null) return "-";
        try {
            ClassGroup group = classRepo.findById(classId);
            return group != null ? group.getName() : "-";
        } catch (SQLException e) {
            return "-";
        }
    }

    // ============ Schüler-Aktionen ============

    private void onEditSelectedStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Schüler aus.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            DialogPane pane = loader.load();
            StudentDetailController controller = loader.getController();

            // Lade den Schüler zum Bearbeiten
            Optional<Student> studentOpt = schoolService.loadStudent(selected.getId());
            if (studentOpt.isPresent()) {
                controller.initForEdit(studentOpt.get());
            } else {
                FxUtils.showWarning("Fehler", "Schüler konnte nicht geladen werden.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Schüler bearbeiten");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Student updated = controller.buildResult();
                schoolService.updateStudent(updated);
                FxUtils.showInfo("Gespeichert", "Schüler wurde aktualisiert.");
                performSearch();
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Schüler konnte nicht bearbeitet werden.", e);
        }
    }

    private void onDeleteSelectedStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Schüler aus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Schüler löschen");
        confirm.setHeaderText("Möchten Sie diesen Schüler wirklich löschen?");
        confirm.setContentText(selected.getFirstName() + " " + selected.getLastName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                schoolService.deleteStudent(selected.getId());
                FxUtils.showInfo("Gelöscht", "Schüler wurde gelöscht.");
                performSearch();
            } catch (Exception e) {
                FxUtils.showError("Fehler", "Schüler konnte nicht gelöscht werden.", e);
            }
        }
    }

    private void onExportStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Schüler aus.");
            return;
        }

        // TODO: Export-Funktionalität implementieren
        FxUtils.showInfo("Export", "Export-Funktion wird demnächst verfügbar sein.");
    }

    // ============ Lehrer-Aktionen ============

    private void onEditSelectedTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Lehrer aus.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/teacher-detail.fxml"));
            DialogPane pane = loader.load();
            TeacherDetailController controller = loader.getController();

            Optional<Teacher> teacherOpt = schoolService.loadTeacher(selected.getId());
            if (teacherOpt.isPresent()) {
                controller.initForEdit(teacherOpt.get());
            } else {
                FxUtils.showWarning("Fehler", "Lehrer konnte nicht geladen werden.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Lehrer bearbeiten");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller.validate()) {
                    Teacher updated = controller.buildResult();
                    schoolService.updateTeacher(updated);
                    FxUtils.showInfo("Gespeichert", "Lehrer wurde aktualisiert.");
                    performSearch();
                }
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Lehrer konnte nicht bearbeitet werden.", e);
        }
    }

    private void onDeleteSelectedTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Lehrer aus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Lehrer löschen");
        confirm.setHeaderText("Möchten Sie diesen Lehrer wirklich löschen?");
        confirm.setContentText(selected.getFirstName() + " " + selected.getLastName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                schoolService.deleteTeacher(selected.getId());
                FxUtils.showInfo("Gelöscht", "Lehrer wurde gelöscht.");
                performSearch();
            } catch (Exception e) {
                FxUtils.showError("Fehler", "Lehrer konnte nicht gelöscht werden.", e);
            }
        }
    }

    private void onExportTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie einen Lehrer aus.");
            return;
        }

        FxUtils.showInfo("Export", "Export-Funktion wird demnächst verfügbar sein.");
    }

    // ============ Klassen-Aktionen ============

    private void onEditSelectedClass() {
        ClassGroup selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie eine Klasse aus.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            DialogPane pane = loader.load();
            ClassDetailController controller = loader.getController();

            Optional<ClassGroup> classOpt = schoolService.loadClass(selected.getId());
            if (classOpt.isPresent()) {
                controller.initForEdit(classOpt.get());
            } else {
                FxUtils.showWarning("Fehler", "Klasse konnte nicht geladen werden.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Klasse bearbeiten");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller.validate()) {
                    ClassGroup updated = new ClassGroup();
                    updated.setId(selected.getId());
                    updated.setName(controller.getName());
                    updated.setGrade(controller.getGrade());
                    updated.setSchoolType(controller.getSchoolType());
                    updated.setRoom(controller.getRoom());
                    var teacherItem = controller.getSelectedTeacher();
                    if (teacherItem != null) {
                        updated.setTeacherId(teacherItem.id);
                    }

                    schoolService.updateClass(updated);
                    FxUtils.showInfo("Gespeichert", "Klasse wurde aktualisiert.");
                    performSearch();
                }
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Klasse konnte nicht bearbeitet werden.", e);
        }
    }

    private void onDeleteSelectedClass() {
        ClassGroup selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie eine Klasse aus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Klasse löschen");
        confirm.setHeaderText("Möchten Sie diese Klasse wirklich löschen?");
        confirm.setContentText(selected.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                schoolService.deleteClass(selected.getId());
                FxUtils.showInfo("Gelöscht", "Klasse wurde gelöscht.");
                performSearch();
            } catch (Exception e) {
                FxUtils.showError("Fehler", "Klasse konnte nicht gelöscht werden.", e);
            }
        }
    }

    private void onShowClassStudents() {
        ClassGroup selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showWarning("Keine Auswahl", "Bitte wählen Sie eine Klasse aus.");
            return;
        }

        try {
            // Filtere Suche auf diese Klasse
            if (filterClassCombo != null) {
                filterClassCombo.getSelectionModel().select(selected.getName());
            }

            // Wechsle zum Schüler-Tab
            if (resultsTabPane != null) {
                resultsTabPane.getSelectionModel().selectFirst();
            }

            // Führe Suche aus
            performSearch();

            FxUtils.showInfo("Filter gesetzt",
                "Zeige jetzt alle Schüler der Klasse " + selected.getName());

        } catch (Exception e) {
            FxUtils.showError("Fehler", "Schüler konnten nicht gefiltert werden.", e);
        }
    }
}

