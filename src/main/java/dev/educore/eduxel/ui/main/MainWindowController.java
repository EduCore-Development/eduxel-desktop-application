package dev.educore.eduxel.ui.main;

import dev.educore.eduxel.meta.EduxelMeta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainWindowController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private VBox navSchoolButton;

    @FXML
    private VBox navInventoryButton;

    @FXML
    private VBox navSettingsButton;

    @FXML
    private TableView<ActivityEntry> activityTable;

    @FXML
    private Label statusLabel;

    private final ObservableList<ActivityEntry> activities = FXCollections.observableArrayList();

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.y HH:mm");

    @FXML
    private void initialize() {
        if (titleLabel != null) {
            titleLabel.setText(EduxelMeta.APP_NAME);
        }
        if (versionLabel != null) {
            versionLabel.setText(EduxelMeta.VERSION + " • " + EduxelMeta.BUILD_CHANNEL);
        }
        if (statusLabel != null) {
            statusLabel.setText("Alle Systeme aktiv");
        }

        if (activityTable != null) {
            setupActivityTable();
            seedDemoActivities();
        }

        setNavSelected(navSchoolButton);
    }

    private void setupActivityTable() {
        if (activityTable.getColumns().size() == 4) {
            TableColumn<ActivityEntry, String> timeCol =
                    (TableColumn<ActivityEntry, String>) activityTable.getColumns().get(0);
            TableColumn<ActivityEntry, String> areaCol =
                    (TableColumn<ActivityEntry, String>) activityTable.getColumns().get(1);
            TableColumn<ActivityEntry, String> actionCol =
                    (TableColumn<ActivityEntry, String>) activityTable.getColumns().get(2);
            TableColumn<ActivityEntry, String> userCol =
                    (TableColumn<ActivityEntry, String>) activityTable.getColumns().get(3);

            timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
            areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
            actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
            userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        }

        activityTable.setItems(activities);
    }

    private void seedDemoActivities() {
        activities.clear();
        activities.add(new ActivityEntry(
                formatNowMinusMinutes(3),
                "Schüler",
                "Neuer Schüler angelegt: Max Mustermann",
                "Sekretariat"
        ));
        activities.add(new ActivityEntry(
                formatNowMinusMinutes(8),
                "Inventar",
                "iPad an Klasse 7a zugewiesen",
                "IT-Admin"
        ));
        activities.add(new ActivityEntry(
                formatNowMinusMinutes(15),
                "Schüler",
                "Adresse aktualisiert: Lena Müller",
                "Sekretariat"
        ));
        activities.add(new ActivityEntry(
                formatNowMinusMinutes(30),
                "System",
                "Nächtliches Backup erfolgreich abgeschlossen",
                EduxelMeta.APP_NAME
        ));
    }

    private String formatNowMinusMinutes(long minutes) {
        LocalDateTime time = LocalDateTime.now().minusMinutes(minutes);
        return TIME_FORMATTER.format(time);
    }

    private void setNavSelected(VBox selected) {
        if (navSchoolButton != null) {
            navSchoolButton.getStyleClass().remove("nav-item-selected");
        }
        if (navInventoryButton != null) {
            navInventoryButton.getStyleClass().remove("nav-item-selected");
        }
        if (navSettingsButton != null) {
            navSettingsButton.getStyleClass().remove("nav-item-selected");
        }
        if (selected != null && !selected.getStyleClass().contains("nav-item-selected")) {
            selected.getStyleClass().add("nav-item-selected");
        }
    }

    @FXML
    private void onNavSchoolClicked() {
        setNavSelected(navSchoolButton);
        addActivity("Schüler", "Schuldatenbank geöffnet", "Emin");
    }

    @FXML
    private void onNavInventoryClicked() {
        setNavSelected(navInventoryButton);
        addActivity("Inventar", "Inventarübersicht geöffnet", "Emin");
    }

    @FXML
    private void onNavSettingsClicked() {
        setNavSelected(navSettingsButton);
        addActivity("System", "Einstellungen geöffnet", "Emin");
    }

    @FXML
    private void onQuickCreateStudent() {
        setNavSelected(navSchoolButton);
        addActivity("Schüler", "Schnellaktion: Neuen Schüler anlegen gestartet", "Emin");
    }

    @FXML
    private void onQuickAssignDevice() {
        setNavSelected(navInventoryButton);
        addActivity("Inventar", "Schnellaktion: Gerät zuweisen gestartet", "Emin");
    }

    @FXML
    private void onQuickOpenClasses() {
        setNavSelected(navSchoolButton);
        addActivity("Schüler", "Schnellaktion: Klassenübersicht geöffnet", "Emin");
    }

    private void addActivity(String area, String action, String user) {
        String time = TIME_FORMATTER.format(LocalDateTime.now());
        activities.add(0, new ActivityEntry(time, area, action, user));
        if (activityTable != null) {
            activityTable.scrollTo(0);
        }
    }
}
