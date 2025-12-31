package dev.educore.eduxel.app;

import dev.educore.eduxel.meta.AppInfo;
import dev.educore.eduxel.service.UpdateChecker;
import dev.educore.eduxel.ui.common.UpdateDialog;
import dev.educore.eduxel.ui.main.MainWindowController;
import dev.educore.eduxel.ui.splash.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EduxelApp extends Application {

    @Override
    public void start(Stage stage) {
        new dev.educore.eduxel.ui.splash.SplashScreen().show(() -> {
            initMainStage(stage);
            checkForUpdates(stage);
        });
    }



    private void initMainStage(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dev/educore/eduxel/ui/main/main-window.fxml")
            );

            Scene scene = new Scene(loader.load());
            stage.setTitle(AppInfo.getWindowTitle());

            try {
                String iconPath = "/dev/educore/eduxel/assets/logo.png";
                Image i16 = new Image(getClass().getResourceAsStream(iconPath), 16, 16, true, true);
                Image i32 = new Image(getClass().getResourceAsStream(iconPath), 32, 32, true, true);
                Image i48 = new Image(getClass().getResourceAsStream(iconPath), 48, 48, true, true);
                Image i64 = new Image(getClass().getResourceAsStream(iconPath), 64, 64, true, true);
                Image i128 = new Image(getClass().getResourceAsStream(iconPath), 128, 128, true, true);
                stage.getIcons().setAll(i16, i32, i48, i64, i128);
            } catch (Exception ignored) {
            }

            stage.setScene(scene);
            stage.setMinWidth(950);
            stage.setMinHeight(650);

            // Initial size
            stage.setWidth(1024);
            stage.setHeight(720);
            
            stage.show();

            MainWindowController controller = loader.getController();
            if (controller != null) {
                controller.onAppReady();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüft asynchron auf verfügbare Updates und zeigt einen Dialog an.
     */
    private void checkForUpdates(Stage mainStage) {
        UpdateChecker.checkForUpdate().thenAccept(updateAvailable -> {
            if (updateAvailable) {
                // Dialog auf dem JavaFX Application Thread anzeigen
                Platform.runLater(() -> {
                    UpdateDialog dialog = new UpdateDialog(mainStage);
                    dialog.show();
                });
            }
        }).exceptionally(ex -> {
            System.err.println("Fehler bei der Update-Prüfung: " + ex.getMessage());
            return null;
        });
    }
}
