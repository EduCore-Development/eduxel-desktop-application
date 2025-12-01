package dev.educore.eduxel.ui.common;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class UpdateDialog {

    private static final String GITHUB_URL = "https://github.com/EduCore-Development/eduxel-desktop-application";
    private static final String DOWNLOAD_URL = "https://edu-core.dev/download";

    private final Stage stage;

    public UpdateDialog(Stage owner) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Update verfügbar");
        stage.setResizable(false);

        VBox root = createContent();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    private VBox createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        // Titel
        Label titleLabel = new Label("🎉 Ein Update ist verfügbar!");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Beschreibung
        Label descriptionLabel = new Label(
                "Eine neue Version von Eduxel ist verfügbar.\n" +
                "Lade das neueste Update herunter, um von neuen Funktionen\n" +
                "und Verbesserungen zu profitieren."
        );
        descriptionLabel.setFont(Font.font("System", 13));
        descriptionLabel.setStyle("-fx-text-fill: #34495e; -fx-text-alignment: center;");
        descriptionLabel.setWrapText(true);

        // Download-Optionen
        Label downloadLabel = new Label("Download-Optionen:");
        downloadLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        downloadLabel.setStyle("-fx-text-fill: #2c3e50;");

        // GitHub Link
        Hyperlink githubLink = new Hyperlink("📦 GitHub Repository");
        githubLink.setFont(Font.font("System", 13));
        githubLink.setStyle("-fx-text-fill: #3498db;");
        githubLink.setOnAction(e -> openURL(GITHUB_URL));

        // Website Link
        Hyperlink websiteLink = new Hyperlink("🌐 Offizielle Website");
        websiteLink.setFont(Font.font("System", 13));
        websiteLink.setStyle("-fx-text-fill: #3498db;");
        websiteLink.setOnAction(e -> openURL(DOWNLOAD_URL));

        VBox linksBox = new VBox(5);
        linksBox.setAlignment(Pos.CENTER);
        linksBox.getChildren().addAll(githubLink, websiteLink);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button laterButton = new Button("Später");
        laterButton.setPrefWidth(100);
        laterButton.setStyle(
                "-fx-background-color: #95a5a6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 8 20 8 20; " +
                "-fx-cursor: hand;"
        );
        laterButton.setOnAction(e -> stage.close());

        Button downloadButton = new Button("Jetzt herunterladen");
        downloadButton.setPrefWidth(150);
        downloadButton.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 20 8 20; " +
                "-fx-cursor: hand;"
        );
        downloadButton.setOnMouseEntered(e ->
                downloadButton.setStyle(
                        "-fx-background-color: #2980b9; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-cursor: hand;"
                )
        );
        downloadButton.setOnMouseExited(e ->
                downloadButton.setStyle(
                        "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-cursor: hand;"
                )
        );
        downloadButton.setOnAction(e -> {
            openURL(DOWNLOAD_URL);
            stage.close();
        });

        buttonBox.getChildren().addAll(laterButton, downloadButton);

        root.getChildren().addAll(
                titleLabel,
                descriptionLabel,
                downloadLabel,
                linksBox,
                buttonBox
        );

        root.setMinWidth(450);
        root.setMinHeight(300);

        return root;
    }

    private void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Öffnen der URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void show() {
        stage.showAndWait();
    }
}

