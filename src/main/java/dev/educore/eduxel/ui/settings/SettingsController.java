package dev.educore.eduxel.ui.settings;

import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.persistence.CredentialBrokerClient;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.persistence.SchemaBootstrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private PasswordField secretField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;
    @FXML private Button saveButton;

    @FXML
    private void initialize() {
        ClientConfig cfg = ClientConfig.load();
        if (cfg.getHost() != null) hostField.setText(cfg.getHost());
        if (cfg.getPort() > 0) portField.setText(String.valueOf(cfg.getPort()));
        // Secret absichtlich leer lassen

        updateSaveEnabled();
        hostField.textProperty().addListener((o, a, b) -> updateSaveEnabled());
        portField.textProperty().addListener((o, a, b) -> updateSaveEnabled());
        secretField.textProperty().addListener((o, a, b) -> updateSaveEnabled());
    }

    private void updateSaveEnabled() {
        if (saveButton != null) saveButton.setDisable(!isSaneInput());
    }

    private boolean isSaneInput() {
        String host = val(hostField);
        Integer port = parseInt(val(portField));
        String sec = secretField.getText() == null ? "" : secretField.getText();
        return isValidHost(host) && isValidPort(port) && isPlausibleSecret(sec);
    }

    @FXML
    private void onTestConnection() {
        if (!isSaneInput()) {
            showError("Bitte gültige Daten eingeben (Host, Port, Secret).");
            return;
        }
        showBusy("Teste Verbindung...");
        final String host = val(hostField);
        final int port = parseInt(val(portField));
        final String sec = secretField.getText();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ClientConfig tmp = ClientConfig.load();
                tmp.setHost(host); tmp.setPort(port); tmp.setSecretPlain(sec);
                new CredentialBrokerClient().fetchCredentials(tmp);
                return null;
            }
        };
        task.setOnSucceeded(e -> showOk("Verbindung erfolgreich"));
        task.setOnFailed(e -> showError("Verbindung fehlgeschlagen: " + optionalMessage(task.getException())));
        new Thread(task, "settings-test").start();
    }

    @FXML
    private void onSave() {
        if (!isSaneInput()) {
            showError("Bitte gültige Daten eingeben (Host, Port, Secret).");
            return;
        }
        showBusy("Speichere und prüfe Schema...");
        final String host = val(hostField);
        final int port = parseInt(val(portField));
        final String sec = secretField.getText();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ClientConfig cfg = ClientConfig.load();
                cfg.setHost(host); cfg.setPort(port); cfg.setSecretPlain(sec);
                // Test holen
                new CredentialBrokerClient().fetchCredentials(cfg);
                // Persist
                cfg.save();
                // Reset DS und Schema prüfen
                DataSourceProvider.reset();
                SchemaBootstrapper.bootstrap();
                return null;
            }
        };
        task.setOnSucceeded(e -> showOk("Gespeichert. Verbindung aktiv."));
        task.setOnFailed(e -> showError("Speichern fehlgeschlagen: " + optionalMessage(task.getException())));
        new Thread(task, "settings-save").start();
    }

    private void showBusy(String msg) {
        if (progress != null) progress.setVisible(true);
        if (statusLabel != null) {
            statusLabel.setStyle("");
            statusLabel.setText(msg);
        }
    }

    private void showError(String msg) {
        if (progress != null) progress.setVisible(false);
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: #d92d20;");
            statusLabel.setText(msg);
        }
    }

    private void showOk(String msg) {
        if (progress != null) progress.setVisible(false);
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
            statusLabel.setText(msg);
        }
    }

    private static String val(TextField tf) { return tf.getText() == null ? "" : tf.getText().trim(); }
    private static Integer parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return null; } }

    // Validation helpers (same logic as dialog)
    private boolean isValidHost(String host) {
        if (host == null || host.isBlank()) return false;
        String h = host.trim();
        if ("localhost".equalsIgnoreCase(h)) return true;
        if (h.matches("^(25[0-5]|2[0-4]\\d|1?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}$")) return true;
        if (h.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")) return true;
        return h.matches("^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))+\\.?$");
    }

    private boolean isValidPort(Integer port) { return port != null && port >= 1 && port <= 65535; }
    private boolean isPlausibleSecret(String s) { return s != null && s.trim().length() >= 8; }

    private String optionalMessage(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m;
    }
}
