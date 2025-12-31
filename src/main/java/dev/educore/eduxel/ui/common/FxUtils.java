package dev.educore.eduxel.ui.common;

import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

public final class FxUtils {
    private FxUtils() {}

    public static void applyCustomStyle(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        try {
            var cssResource = FxUtils.class.getResource("/dev/educore/eduxel/ui/main/main-theme.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
            }
        } catch (Exception e) {
            // Fallback: Stylesheet konnte nicht geladen werden, Dialog trotzdem anzeigen
            System.err.println("Could not load dialog stylesheet: " + e.getMessage());
        }
        dialogPane.getStyleClass().add("custom-dialog");
    }

    private static void setIcon(Alert alert, String iconLiteral, String colorClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(32);
        icon.getStyleClass().add(colorClass);
        alert.setGraphic(icon);
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        applyCustomStyle(a);
        setIcon(a, "mdi2h-help-circle-outline", "info-icon");
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    public static Optional<String> promptText(String title, String header, String content, String defaultValue) {
        TextInputDialog dlg = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dlg.setTitle(title);
        dlg.setHeaderText(header);
        dlg.setContentText(content);
        applyCustomStyle(dlg);
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
        applyCustomStyle(a);
        setIcon(a, "mdi2i-information-outline", "info-icon");
        a.showAndWait();
    }

    public static void showWarning(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        applyCustomStyle(a);
        setIcon(a, "mdi2a-alert-outline", "warning-icon");
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
        applyCustomStyle(a);
        setIcon(a, "mdi2a-alert-circle-outline", "error-icon");
        a.showAndWait();
    }
}
