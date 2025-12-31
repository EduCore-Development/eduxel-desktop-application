package dev.educore.eduxel.ui.settings;

import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.meta.EduxelMeta;
import dev.educore.eduxel.persistence.CredentialBrokerClient;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.persistence.SchemaBootstrapper;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;

public class SettingsController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private PasswordField secretField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;
    @FXML private Button saveButton;

    @FXML private Label buildInfoLabel;

    private static final String BUG_REPORT_URL = "https://github.com/EduCore-Development/eduxel-desktop-application/issues/new";
    private static final String GITHUB_URL = "https://github.com/EduCore-Development/eduxel-desktop-application";

    @FXML
    private void initialize() {
        ClientConfig cfg = ClientConfig.load();
        if (cfg.getHost() != null) hostField.setText(cfg.getHost());
        if (cfg.getPort() > 0) portField.setText(String.valueOf(cfg.getPort()));

        String buildInfo = EduxelMeta.APP_NAME + " " + EduxelMeta.VERSION +
                " • " + EduxelMeta.BUILD_CHANNEL + " – " + EduxelMeta.VENDOR;
        if (buildInfoLabel != null) buildInfoLabel.setText(buildInfo);

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
                tmp.setHost(host);
                tmp.setPort(port);
                tmp.setSecretPlain(sec);
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
                cfg.setHost(host);
                cfg.setPort(port);
                cfg.setSecretPlain(sec);
                new CredentialBrokerClient().fetchCredentials(cfg);
                cfg.save();
                DataSourceProvider.reset();
                SchemaBootstrapper.bootstrap();
                return null;
            }
        };
        task.setOnSucceeded(e -> showOk("Gespeichert. Verbindung aktiv."));
        task.setOnFailed(e -> showError("Speichern fehlgeschlagen: " + optionalMessage(task.getException())));
        new Thread(task, "settings-save").start();
    }

    @FXML
    private void onOpenBugReport() {
        openUrl(BUG_REPORT_URL);
    }

    @FXML
    private void onOpenGithub() {
        openUrl(GITHUB_URL);
    }

    @FXML
    private void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Ausloggen & Zurücksetzen");
        confirm.setHeaderText("Möchten Sie sich wirklich ausloggen?");
        confirm.setContentText(
            "Alle gespeicherten Einstellungen (Server-IP, Port, Secret) werden gelöscht.\n\n" +
            "Die Datenbank bleibt unverändert.\n" +
            "Sie müssen sich danach neu anmelden."
        );
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        FxUtils.applyCustomStyle(confirm);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        try {
            // Lösche gespeicherte Konfiguration
            ClientConfig.clearConfig();

            // Setze DataSource zurück
            DataSourceProvider.reset();

            // Leere Felder
            if (hostField != null) hostField.clear();
            if (portField != null) portField.clear();
            if (secretField != null) secretField.clear();

            showOk("Erfolgreich ausgeloggt. Bitte neue Verbindung konfigurieren.");

            // Optional: Info-Dialog
            FxUtils.showInfo("Ausgeloggt", "Sie wurden erfolgreich ausgeloggt. Bitte konfigurieren Sie eine neue Verbindung zum Server.");

        } catch (Exception e) {
            showError("Fehler beim Ausloggen: " + optionalMessage(e));
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showError("Konnte Browser nicht öffnen: " + optionalMessage(e));
        }
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

    private static String val(TextField tf) {
        return tf.getText() == null ? "" : tf.getText().trim();
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidHost(String host) {
        if (host == null || host.isBlank()) return false;
        String h = host.trim();
        if ("localhost".equalsIgnoreCase(h)) return true;
        if (h.matches("^(25[0-5]|2[0-4]\\d|1?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}$")) return true;
        if (h.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")) return true;
        return h.matches("^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))+\\.?$");
    }

    private boolean isValidPort(Integer port) {
        return port != null && port >= 1 && port <= 65535;
    }

    private boolean isPlausibleSecret(String s) {
        return s != null && s.trim().length() >= 8;
    }

    private String optionalMessage(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m;
    }
}
