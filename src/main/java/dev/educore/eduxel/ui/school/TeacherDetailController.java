package dev.educore.eduxel.ui.school;

import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;

public class TeacherDetailController {

    @FXML private DialogPane dialogPane;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtSubject;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    private final SchoolService schoolService = new SchoolService();

    private Teacher editingTeacher;

    public void initForCreate() {
        this.editingTeacher = null;
    }

    public void initForEdit(Teacher teacher) {
        this.editingTeacher = teacher;
        txtFirstName.setText(teacher.getFirstName());
        txtLastName.setText(teacher.getLastName());
        txtSubject.setText(teacher.getSubject());
        txtEmail.setText(teacher.getEmail());
        txtPhone.setText(teacher.getPhone());
    }

    public Teacher buildResult() {
        if (editingTeacher == null) {
            editingTeacher = new Teacher();
        }
        editingTeacher.setFirstName(getFirstName());
        editingTeacher.setLastName(getLastName());
        editingTeacher.setSubject(getSubject());
        editingTeacher.setEmail(getEmail());
        editingTeacher.setPhone(getPhone());
        return editingTeacher;
    }

    public String getFirstName() {
        return txtFirstName.getText() == null ? "" : txtFirstName.getText().trim();
    }

    public String getLastName() {
        return txtLastName.getText() == null ? "" : txtLastName.getText().trim();
    }

    public String getSubject() {
        return txtSubject.getText();
    }

    public String getEmail() {
        return txtEmail.getText();
    }

    public String getPhone() {
        return txtPhone.getText();
    }

    public boolean validate() {
        if (getFirstName().isBlank() || getLastName().isBlank()) {
            FxUtils.showError("Ungültige Eingabe", "Vorname und Nachname sind Pflichtfelder.", null);
            return false;
        }
        return true;
    }
}
