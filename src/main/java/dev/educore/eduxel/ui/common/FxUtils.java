package dev.educore.eduxel.ui.common;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public final class FxUtils {
    private FxUtils() {}

    public static Optional<String> promptText(String title, String header, String content, String defaultValue) {
        TextInputDialog dlg = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        dlg.setContentText(content);
        return dlg.showAndWait();
    }

    public static Optional<Integer> promptInt(String title, String header, String content, Integer defaultValue) {
        Optional<String> res = promptText(title, header, content, defaultValue == null ? null : String.valueOf(defaultValue));
        if (res.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(res.get().trim()));
        } catch (NumberFormatException e) {
            showError("Ungültige Zahl", "Bitte eine gültige Zahl eingeben.", null);
            return Optional.empty();
        }
    }

    public static void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public static void showError(String title, String content, Throwable t) {
        StringBuilder msg = new StringBuilder();
        if (content != null && !content.isEmpty()) {
            msg.append(content);
        }
        if (t != null) {
            if (msg.length() > 0) {
                msg.append("\n\n");
            }
            msg.append(t.toString());
        }
        if (msg.length() == 0) {
            msg.append("Unbekannter Fehler");
        }
        Alert a = new Alert(Alert.AlertType.ERROR, msg.toString(), ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
