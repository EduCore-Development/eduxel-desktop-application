package dev.educore.eduxel.ui.web;

import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.domain.user.WebAccount;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.persistence.school.StudentRepository;
import dev.educore.eduxel.persistence.school.TeacherRepository;
import dev.educore.eduxel.persistence.web.WebAccountRepository;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WebDashboardController {

    @FXML private VBox setupBox;
    @FXML private VBox dashboardBox;
    @FXML private PasswordField secretField;
    @FXML private Label totalAccountsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label sickStudentsLabel;
    @FXML private Label missingStudentsLabel;
    @FXML private Label totalClassesLabel;

    @FXML private TextField userEmailField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private ComboBox<Object> personSelectionCombo;

    private final StudentRepository studentRepo = new StudentRepository();
    private final TeacherRepository teacherRepo = new TeacherRepository();
    private final ClassGroupRepository classRepo = new ClassGroupRepository();
    private final WebAccountRepository webAccountRepo = new WebAccountRepository();

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private final SecureRandom random = new SecureRandom();

    @FXML
    public void initialize() {
        userTypeCombo.setItems(FXCollections.observableArrayList("Schüler", "Lehrer", "Eltern"));
        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updatePersonSelection(newVal);
        });

        ClientConfig cfg = ClientConfig.load();
        if (cfg.isWebDashboardConfigured()) {
            setupBox.setVisible(false);
            dashboardBox.setVisible(true);
            loadStatistics();
        } else {
            setupBox.setVisible(true);
            dashboardBox.setVisible(false);
        }
    }

    @FXML
    private void onOpenRepo() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/EduCore-Dev/Eduxel-Web-Dashboard"));
        } catch (Exception e) {
            FxUtils.showError("Link konnte nicht geöffnet werden", "Repository-Link", e);
        }
    }

    @FXML
    private void onSetup() {
        String secret = secretField.getText();
        if (secret == null || secret.isBlank()) {
            FxUtils.showError("Eingabe erforderlich", "Bitte geben Sie ein Secret ein.", null);
            return;
        }
        
        ClientConfig cfg = ClientConfig.load();
        cfg.setWebDashboardSecretPlain(secret);
        cfg.save();

        setupBox.setVisible(false);
        dashboardBox.setVisible(true);
        loadStatistics();
        FxUtils.showInfo("Erfolgreich", "Das Web Dashboard wurde erfolgreich konfiguriert.");
    }

    private void loadStatistics() {
        Platform.runLater(() -> {
            try {
                totalAccountsLabel.setText(String.valueOf(webAccountRepo.countAll()));
                totalTeachersLabel.setText(String.valueOf(teacherRepo.countAll()));
                sickStudentsLabel.setText(String.valueOf(studentRepo.countSick()));
                missingStudentsLabel.setText(String.valueOf(studentRepo.countMissingUnexcused()));
                totalClassesLabel.setText(String.valueOf(classRepo.countAll()));
            } catch (SQLException e) {
                FxUtils.showError("Fehler beim Laden der Statistiken", e.getMessage(), e);
            }
        });
    }

    private void updatePersonSelection(String type) {
        if (type == null) return;
        try {
            if (type.equals("Schüler")) {
                personSelectionCombo.setItems(FXCollections.observableArrayList(studentRepo.listAllItems()));
            } else if (type.equals("Lehrer")) {
                personSelectionCombo.setItems(FXCollections.observableArrayList(teacherRepo.listAll()));
            } else {
                // Eltern werden aktuell über Schüler zugeordnet (Simulation der Auswahl von Schülern für Eltern-Account)
                personSelectionCombo.setItems(FXCollections.observableArrayList(studentRepo.listAllItems()));
            }
        } catch (SQLException e) {
            FxUtils.showError("Fehler beim Laden der Personen", e.getMessage(), e);
        }
    }

    @FXML
    private void onGeneratePassword() {
        String password = IntStream.range(0, 12)
                .map(i -> random.nextInt(CHARS.length()))
                .mapToObj(i -> String.valueOf(CHARS.charAt(i)))
                .collect(Collectors.joining());
        passwordField.setText(password);
    }

    @FXML
    private void onAssignParents() {
        FxUtils.showInfo("Eltern-Zuordnung", "Wählen Sie einen Schüler aus, um für dessen Eltern einen Account zu erstellen.");
    }

    @FXML
    private void onCreateAccount() {
        String email = userEmailField.getText();
        Object selected = personSelectionCombo.getSelectionModel().getSelectedItem();
        String type = userTypeCombo.getSelectionModel().getSelectedItem();

        if (email.isEmpty() || selected == null || type == null) {
            FxUtils.showError("Fehlende Daten", "Bitte E-Mail, Typ und Person auswählen.", null);
            return;
        }

        try {
            WebAccount acc = new WebAccount();
            acc.setEmail(email);
            acc.setPasswordHash(passwordField.getText()); // In einer echten App natürlich hashen!
            acc.setType(type);
            
            if (selected instanceof StudentRepository.StudentItem si) {
                acc.setReferenceId(si.id());
            } else if (selected instanceof TeacherRepository.TeacherItem ti) {
                acc.setReferenceId(ti.id);
            }

            webAccountRepo.create(acc);
            
            FxUtils.showInfo("Account erstellt", "Der Account für " + selected + " wurde angelegt.\nLogin: " + email);
            userEmailField.clear();
            passwordField.clear();
            loadStatistics();
        } catch (SQLException e) {
            FxUtils.showError("Fehler beim Erstellen des Accounts", e.getMessage(), e);
        }
    }
}
