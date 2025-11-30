package dev.educore.eduxel.ui.common;

import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.persistence.CredentialBrokerClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Moderner Konfigurationsdialog für Server-IP/Host, Port und Secret, mit Live-Validierung
 * und direktem Verbindungstest vor dem Speichern.
 */
public class ServerConfigDialog extends Dialog<ClientConfig> {

    private final TextField hostField = new TextField();
    private final TextField portField = new TextField();
    private final PasswordField secretField = new PasswordField();

    private final Label statusLabel = new Label();
    private final ProgressIndicator progress = new ProgressIndicator();

    public ServerConfigDialog(ClientConfig existing) {
        setTitle("Eduxel Server");
        setHeaderText("Verbindung zum Eduxel‑Server einrichten");

        // Logo oben links im Dialog
        try {
            Image img = new Image(getClass().getResourceAsStream("/dev/educore/eduxel/assets/logo.png"), 40, 40, true, true);
            getDialogPane().setGraphic(new ImageView(img));
        } catch (Exception ignored) {}

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        hostField.setPromptText("z. B. 192.168.1.10 oder server.schule.de");
        portField.setPromptText("45821");
        secretField.setPromptText("Secret");

        if (existing != null) {
            if (existing.getHost() != null) hostField.setText(existing.getHost());
            if (existing.getPort() > 0) portField.setText(String.valueOf(existing.getPort()));
            // Secret nicht vor-ausfüllen
        }

        grid.add(new Label("Server‑IP/Host"), 0, 0); grid.add(hostField, 1, 0);
        grid.add(new Label("Port"), 0, 1); grid.add(portField, 1, 1);
        grid.add(new Label("Secret"), 0, 2); grid.add(secretField, 1, 2);

        HBox statusBox = new HBox(8, progress, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        progress.setVisible(false);
        progress.setPrefSize(18, 18);
        grid.add(statusBox, 0, 3, 2, 1);

        Button testBtn = new Button("Verbindung testen");
        testBtn.getStyleClass().add("pill-button");
        HBox footer = new HBox(testBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(6, 0, 0, 0));
        grid.add(footer, 0, 4, 2, 1);

        getDialogPane().setContent(grid);

        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        Runnable updateOkDisable = () -> okButton.setDisable(!isSaneInput());
        hostField.textProperty().addListener((obs, o, n) -> updateOkDisable.run());
        portField.textProperty().addListener((obs, o, n) -> updateOkDisable.run());
        secretField.textProperty().addListener((obs, o, n) -> updateOkDisable.run());

        testBtn.setOnAction(e -> runConnectivityTest(false));

        ((Button) okButton).addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!isSaneInput()) {
                showError("Bitte gültige Daten eingeben (Host, Port, Secret).");
                evt.consume();
                return;
            }
            boolean success = runConnectivityTest(true);
            if (!success) {
                evt.consume();
            }
        });

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                ClientConfig cfg = ClientConfig.load();
                cfg.setHost(hostField.getText().trim());
                cfg.setPort(Integer.parseInt(portField.getText().trim()));
                cfg.setSecretPlain(secretField.getText());
                return cfg;
            }
            return null;
        });

        Platform.runLater(hostField::requestFocus);
    }

    private boolean isSaneInput() {
        String host = hostField.getText() == null ? "" : hostField.getText().trim();
        String portStr = portField.getText() == null ? "" : portField.getText().trim();
        String sec = secretField.getText() == null ? "" : secretField.getText();
        Integer port = null;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
        return isValidHost(host) && isValidPort(port) && isPlausibleSecret(sec);
    }

    private void showBusy(String msg) {
        progress.setVisible(true);
        statusLabel.setStyle("");
        statusLabel.setText(msg);
    }

    private void showError(String msg) {
        progress.setVisible(false);
        statusLabel.setStyle("-fx-text-fill: #d92d20; ");
        statusLabel.setText(msg);
    }

    private void showOk(String msg) {
        progress.setVisible(false);
        statusLabel.setStyle("-fx-text-fill: #16a34a; ");
        statusLabel.setText(msg);
    }

    /**
     * @param blockIfInline true = synchron (für OK), false = asynchron (für Test-Button)
     */
    private boolean runConnectivityTest(boolean blockIfInline) {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String sec = secretField.getText();

        if (blockIfInline) {
            showBusy("Teste Verbindung...");
            try {
                ClientConfig tmp = ClientConfig.load();
                tmp.setHost(host); tmp.setPort(port); tmp.setSecretPlain(sec);
                new CredentialBrokerClient().fetchCredentials(tmp);
                showOk("Verbindung erfolgreich");
                return true;
            } catch (Exception ex) {
                showError("Verbindung fehlgeschlagen: " + optionalMessage(ex));
                return false;
            }
        } else {
            showBusy("Teste Verbindung...");
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    ClientConfig tmp = ClientConfig.load();
                    tmp.setHost(host); tmp.setPort(port); tmp.setSecretPlain(sec);
                    new CredentialBrokerClient().fetchCredentials(tmp);
                    return null;
                }
            };
            t.setOnSucceeded(ev -> showOk("Verbindung erfolgreich"));
            t.setOnFailed(ev -> showError("Verbindung fehlgeschlagen: " + optionalMessage(t.getException())));
            new Thread(t, "broker-test").start();
            return false;
        }
    }

    private String optionalMessage(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m;
    }

    // Local lightweight validation helpers to ensure dialog compiles independently
    private boolean isValidHost(String host) {
        if (host == null || host.isBlank()) return false;
        String h = host.trim();
        if ("localhost".equalsIgnoreCase(h)) return true;
        // IPv4
        if (h.matches("^(25[0-5]|2[0-4]\\d|1?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}$")) return true;
        // Simple IPv6 (not exhaustive)
        if (h.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")) return true;
        // Hostname with at least one dot
        return h.matches("^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))+\\.?$");
    }

    private boolean isValidPort(Integer port) {
        return port != null && port >= 1 && port <= 65535;
    }

    private boolean isPlausibleSecret(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.length() >= 8; // simple minimum length check
    }
}
