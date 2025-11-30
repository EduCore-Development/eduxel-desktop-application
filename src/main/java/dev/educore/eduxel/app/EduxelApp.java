package dev.educore.eduxel.app;

import dev.educore.eduxel.meta.AppInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EduxelApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/dev/educore/eduxel/ui/main/main-window.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle(AppInfo.getWindowTitle());
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }
}
