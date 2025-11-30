package dev.educore.eduxel.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public final class NavigationManager {

    private static BorderPane root;

    private NavigationManager() {
    }

    public static void setRoot(BorderPane borderPane) {
        root = borderPane;
    }

    public static void showStudentOverview() {
        setCenter("/dev/educore/eduxel/ui/school/student-overview.fxml");
    }

    public static void showSchoolDatabaseOverview() {
        setCenter("/dev/educore/eduxel/ui/school/school-database-overview.fxml");
    }

    public static void showInventoryOverview() {
        setCenter("/dev/educore/eduxel/ui/inventory/inventory-overview.fxml");
    }

    public static void showSettings() {
        setCenter("/dev/educore/eduxel/ui/settings/settings.fxml");
    }

    private static void setCenter(String fxmlPath) {
        if (root == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Node content = loader.load();
            root.setCenter(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
