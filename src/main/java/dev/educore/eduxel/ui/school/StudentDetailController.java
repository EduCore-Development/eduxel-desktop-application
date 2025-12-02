package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class StudentDetailController {

    @FXML private DialogPane dialogPane;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<ClassGroupRepository.ClassItem> cbClass;

    @FXML private TextField txtStreet;
    @FXML private TextField txtPostalCode;
    @FXML private TextField txtCity;
    @FXML private TextField txtCountry;
    @FXML private TextField txtStudentEmail;
    @FXML private TextField txtStudentMobile;

    @FXML private TextField txtGuardian1Name;
    @FXML private TextField txtGuardian1Relation;
    @FXML private TextField txtGuardian1Mobile;
    @FXML private TextField txtGuardian1WorkPhone;
    @FXML private TextField txtGuardian1Email;

    @FXML private TextField txtGuardian2Name;
    @FXML private TextField txtGuardian2Relation;
    @FXML private TextField txtGuardian2Mobile;
    @FXML private TextField txtGuardian2Email;

    @FXML private TextArea txtNotes;

    private final SchoolService schoolService = new SchoolService();

    private Student editingStudent;

    @FXML
    private void initialize() {
        reloadClassItems();

        cbClass.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && "__NEW__".equals(newVal.name)) {
                onCreateNewClass();
            }
        });
    }

    private void reloadClassItems() {
        try {
            ClassGroupRepository repo = new ClassGroupRepository();
            var fromDb = repo.listAll();
            var items = new ArrayList<ClassGroupRepository.ClassItem>();
            items.addAll(fromDb);
            // Spezieller Platzhalter für "Neue Klasse" per name="__NEW__"
            items.add(new ClassGroupRepository.ClassItem(-1, "__NEW__", null));
            cbClass.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Klassen", null, e);
        }
    }

    private void onCreateNewClass() {
        // Name abfragen
        var nameOpt = FxUtils.promptText("Neue Klasse", "Neue Klasse anlegen", "Name der Klasse (z.B. 7a):", "");
        if (nameOpt.isEmpty() || nameOpt.get().trim().isEmpty()) {
            cbClass.getSelectionModel().clearSelection();
            return;
        }
        String name = nameOpt.get().trim();

        // Jahrgang optional
        var gradeOpt = FxUtils.promptInt("Neue Klasse", "Jahrgang (optional)", "Jahrgang (z.B. 7):", null);

        try {
            Integer grade = gradeOpt.orElse(null);
            long id = schoolService.createClass(name, grade);

            // Liste neu laden und neue Klasse auswählen
            reloadClassItems();
            cbClass.getItems().stream()
                    .filter(ci -> ci.id == id)
                    .findFirst()
                    .ifPresent(ci -> cbClass.getSelectionModel().select(ci));
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Anlegen der Klasse", null, e);
            cbClass.getSelectionModel().clearSelection();
        }
    }

    public void initForCreate() {
        this.editingStudent = null;
    }

    public void setStudentId(Long studentId) {
        if (studentId == null) {
            initForCreate();
            return;
        }
        try {
            Student s = schoolService.loadStudent(studentId).orElse(null);
            initForEdit(s);
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden des Schülers", null, e);
            initForCreate();
        }
    }

    public void initForEdit(Student s) {
        this.editingStudent = s;
        if (s == null) return;
        txtFirstName.setText(s.getFirstName());
        txtLastName.setText(s.getLastName());

        if (s.getClassId() != null) {
            cbClass.getItems().stream()
                    .filter(ci -> ci.id == s.getClassId())
                    .findFirst()
                    .ifPresent(cbClass.getSelectionModel()::select);
        }

        txtStreet.setText(s.getStreet());
        txtPostalCode.setText(s.getPostalCode());
        txtCity.setText(s.getCity());
        txtCountry.setText(s.getCountry());

        txtStudentEmail.setText(s.getStudentEmail());
        txtStudentMobile.setText(s.getStudentMobile());

        txtGuardian1Name.setText(s.getGuardian1Name());
        txtGuardian1Relation.setText(s.getGuardian1Relation());
        txtGuardian1Mobile.setText(s.getGuardian1Mobile());
        txtGuardian1WorkPhone.setText(s.getGuardian1WorkPhone());
        txtGuardian1Email.setText(s.getGuardian1Email());

        txtGuardian2Name.setText(s.getGuardian2Name());
        txtGuardian2Relation.setText(s.getGuardian2Relation());
        txtGuardian2Mobile.setText(s.getGuardian2Mobile());
        txtGuardian2Email.setText(s.getGuardian2Email());

        txtNotes.setText(s.getNotes());
    }

    public Student buildResult() {
        if (editingStudent == null) {
            editingStudent = new Student();
        }
        editingStudent.setFirstName(txtFirstName.getText() == null ? "" : txtFirstName.getText().trim());
        editingStudent.setLastName(txtLastName.getText() == null ? "" : txtLastName.getText().trim());

        var selectedClass = cbClass.getSelectionModel().getSelectedItem();
        if (selectedClass != null && selectedClass.id > 0 && !"__NEW__".equals(selectedClass.name)) {
            editingStudent.setClassId(selectedClass.id);
        } else {
            editingStudent.setClassId(null);
        }

        editingStudent.setStreet(txtStreet.getText());
        editingStudent.setPostalCode(txtPostalCode.getText());
        editingStudent.setCity(txtCity.getText());
        editingStudent.setCountry(txtCountry.getText());

        editingStudent.setStudentEmail(txtStudentEmail.getText());
        editingStudent.setStudentMobile(txtStudentMobile.getText());

        editingStudent.setGuardian1Name(txtGuardian1Name.getText());
        editingStudent.setGuardian1Relation(txtGuardian1Relation.getText());
        editingStudent.setGuardian1Mobile(txtGuardian1Mobile.getText());
        editingStudent.setGuardian1WorkPhone(txtGuardian1WorkPhone.getText());
        editingStudent.setGuardian1Email(txtGuardian1Email.getText());

        editingStudent.setGuardian2Name(txtGuardian2Name.getText());
        editingStudent.setGuardian2Relation(txtGuardian2Relation.getText());
        editingStudent.setGuardian2Mobile(txtGuardian2Mobile.getText());
        editingStudent.setGuardian2Email(txtGuardian2Email.getText());

        editingStudent.setNotes(txtNotes.getText());

        return editingStudent;
    }
}
