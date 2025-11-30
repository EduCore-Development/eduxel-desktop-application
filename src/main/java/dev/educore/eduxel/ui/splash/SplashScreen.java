package dev.educore.eduxel.ui.splash;

import dev.educore.eduxel.meta.EduxelMeta;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    public void show(Runnable onFinished) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);

        String iconPath = "/dev/educore/eduxel/assets/logo.png";
        try {
            Image i16 = new Image(getClass().getResourceAsStream(iconPath), 16, 16, true, true);
            Image i32 = new Image(getClass().getResourceAsStream(iconPath), 32, 32, true, true);
            Image i48 = new Image(getClass().getResourceAsStream(iconPath), 48, 48, true, true);
            Image i64 = new Image(getClass().getResourceAsStream(iconPath), 64, 64, true, true);
            Image i128 = new Image(getClass().getResourceAsStream(iconPath), 128, 128, true, true);
            stage.getIcons().setAll(i16, i32, i48, i64, i128);
        } catch (Exception ignored) {
        }

        StackPane root = new StackPane();
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 34, 26, 34));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #f9fafb, #eef2ff);" +
                        "-fx-background-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.22), 22, 0.32, 0, 8);"
        );

        ImageView logoView;
        try {
            Image logo = new Image(getClass().getResourceAsStream(iconPath), 96, 96, true, true);
            logoView = new ImageView(logo);
        } catch (Exception e) {
            logoView = new ImageView();
        }
        logoView.setFitWidth(96);
        logoView.setFitHeight(96);
        logoView.setPreserveRatio(true);

        Label title = new Label(EduxelMeta.APP_NAME);
        title.setStyle("-fx-font-size: 22; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label version = new Label("Version " + EduxelMeta.VERSION + " • " + EduxelMeta.BUILD_CHANNEL);
        version.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");

        Label vendor = new Label("by " + EduxelMeta.VENDOR);
        vendor.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");

        Label credits = new Label("Made with ♥ by Ruben & Eministar & Contributors");
        credits.setStyle("-fx-font-size: 11; -fx-text-fill: #4f46e5;");

        card.getChildren().addAll(logoView, title, version, vendor, credits);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 520, 320);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);

        card.setOpacity(0);
        card.setScaleX(0.94);
        card.setScaleY(0.94);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition intro = new ParallelTransition(fadeIn);

        PauseTransition stay = new PauseTransition(Duration.seconds(1.6));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(260), card);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(intro, stay, fadeOut);
        sequence.setOnFinished(e -> {
            stage.close();
            if (onFinished != null) onFinished.run();
        });

        stage.show();
        sequence.play();
    }
}
