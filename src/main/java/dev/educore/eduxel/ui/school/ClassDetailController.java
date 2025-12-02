package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.persistence.school.TeacherRepository;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;

public class ClassDetailController {

    @FXML private DialogPane dialogPane;

    @FXML private TextField txtName;
    @FXML private TextField txtGrade;
    @FXML private TextField txtSchoolType;
    @FXML private TextField txtRoom;

    @FXML private ComboBox<TeacherRepository.TeacherItem> cbTeacher;

    @FXML
    private void initialize() {
        // Lehrer für ComboBox laden
        try {
            TeacherRepository repo = new TeacherRepository();
            cbTeacher.setItems(FXCollections.observableArrayList(repo.listAll()));
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Lehrer", null, e);
        }
    }

    // --- Neue Hilfsmethoden für Edit-Szenario ---
    public TextField getNameField() { return txtName; }
    public TextField getGradeField() { return txtGrade; }

    public String getName() { return txtName.getText() == null ? "" : txtName.getText().trim(); }

    public Integer getGrade() {
        String v = txtGrade.getText();
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            FxUtils.showError("Ungültige Zahl", "Bitte einen gültigen Jahrgang angeben.", null);
            return null;
        }
    }

    public String getSchoolType() { return txtSchoolType.getText(); }

    public String getRoom() { return txtRoom.getText(); }

    public TeacherRepository.TeacherItem getSelectedTeacher() {
        return cbTeacher.getSelectionModel().getSelectedItem();
    }

    public boolean validate() {
        if (getName().isBlank()) {
            FxUtils.showError("Ungültige Eingabe", "Der Klassenname ist ein Pflichtfeld.", null);
            return false;
        }
        return true;
    }

    public void setClassId(Long classId) {
        if (classId == null) return;
        try {
            ClassGroupRepository repo = new ClassGroupRepository();
            ClassGroup group = repo.findById(classId);
            if (group != null) {
                initForEdit(group);
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler beim Laden der Klasse", null, e);
        }
    }

    public void initForEdit(ClassGroup group) {
        txtName.setText(group.getName());
        if (group.getGrade() != null) {
            txtGrade.setText(String.valueOf(group.getGrade()));
        } else {
            txtGrade.clear();
        }
        txtSchoolType.setText(group.getSchoolType());
        txtRoom.setText(group.getRoom());

        if (group.getTeacherId() != null && cbTeacher.getItems() != null) {
            cbTeacher.getSelectionModel().clearSelection();
            for (TeacherRepository.TeacherItem item : cbTeacher.getItems()) {
                if (item.id == group.getTeacherId()) {
                    cbTeacher.getSelectionModel().select(item);
                    break;
                }
            }
        } else {
            cbTeacher.getSelectionModel().clearSelection();
        }
    }
}
