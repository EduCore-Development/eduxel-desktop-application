package dev.educore.eduxel.app;

import dev.educore.eduxel.meta.AppInfo;
import dev.educore.eduxel.ui.main.MainWindowController;
import dev.educore.eduxel.ui.splash.SplashScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EduxelApp extends Application {

    @Override
    public void start(Stage stage) {
        new dev.educore.eduxel.ui.splash.SplashScreen().show(() -> {
            initMainStage(stage);
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
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.show();

            MainWindowController controller = loader.getController();
            if (controller != null) {
                controller.onAppReady();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
