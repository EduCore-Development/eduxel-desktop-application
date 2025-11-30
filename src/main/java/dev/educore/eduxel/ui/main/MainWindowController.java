package dev.educore.eduxel.ui.main;

import dev.educore.eduxel.meta.EduxelMeta;
import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.persistence.SchemaBootstrapper;
import dev.educore.eduxel.persistence.DataSourceProvider;
import dev.educore.eduxel.navigation.NavigationManager;
import dev.educore.eduxel.service.ActivityLogger;
import dev.educore.eduxel.service.ReportingService;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.service.InventoryService;
import dev.educore.eduxel.ui.common.FxUtils;
import dev.educore.eduxel.ui.common.ServerConfigDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindowController {

    @FXML
    private BorderPane root;

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
    private VBox navActivitiesButton;

    @FXML
    private TableView<ActivityEntry> activityTable;

    @FXML
    private Label statusLabel;

    private final ObservableList<ActivityEntry> activities = FXCollections.observableArrayList();

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.y HH:mm");

    @FXML
    private void initialize() {
        // Set Navigation root so other views can be shown in the center
        if (root != null) {
            NavigationManager.setRoot(root);
        }
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
            // actual activities will be loaded on onAppReady()
        }
        // Default: show Settings view in center
        NavigationManager.showSettings();
        setNavSelected(navSettingsButton);
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
                formatNowMinusMinutes(1),
                "System",
                "Willkommen bei Eduxel",
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
        if (navActivitiesButton != null) {
            navActivitiesButton.getStyleClass().remove("nav-item-selected");
        }
        if (selected != null && !selected.getStyleClass().contains("nav-item-selected")) {
            selected.getStyleClass().add("nav-item-selected");
        }
    }

    // Called from App after stage is shown
    public void onAppReady() {
        // Ensure broker configuration
        ClientConfig cfg = ClientConfig.load();
        if (!cfg.isConfigured()) {
            // Navigate to settings to let user configure within the app
            NavigationManager.showSettings();
            setNavSelected(navSettingsButton);
            seedDemoActivities();
            return;
        }
        try {
            SchemaBootstrapper.bootstrap();
            // Load default activities
            refreshActivities();
            ActivityLogger.log("System", "App gestartet", EduxelMeta.APP_NAME);
        } catch (Exception e) {
            FxUtils.showError("Datenbank-Initialisierung fehlgeschlagen",
                    "Bitte Verbindung prüfen (Server-IP/Port/Secret).", e);
            NavigationManager.showSettings();
            setNavSelected(navSettingsButton);
            seedDemoActivities();
        }
    }

    // Legacy dialog flow removed in favor of in-app settings page
    private boolean promptConfigureBroker(ClientConfig cfg) { return false; }

    private void refreshActivities() {
        try {
            ReportingService svc = new ReportingService();
            List<ActivityEntry> latest = svc.listRecentActivities(100);
            activities.setAll(latest);
        } catch (Exception e) {
            // fallback to demo
            seedDemoActivities();
        }
    }

    @FXML
    private void onNavActivitiesClicked() {
        setNavSelected(navActivitiesButton);
        refreshActivities();
    }

    @FXML
    private void onNavSchoolClicked() {
        setNavSelected(navSchoolButton);
        try {
            NavigationManager.showSchoolDatabaseOverview();
            addActivity("Schüler", "Schuldatenbank geöffnet", "Emin");
        } catch (Exception e) {
            FxUtils.showError("Ansicht konnte nicht geladen werden", "Schuldatenbank", e);
        }
    }

    @FXML
    private void onNavInventoryClicked() {
        setNavSelected(navInventoryButton);
        addActivity("Inventar", "Inventarübersicht geöffnet", "Emin");
    }

    @FXML
    private void onNavSettingsClicked() {
        setNavSelected(navSettingsButton);
        NavigationManager.showSettings();
        addActivity("System", "Einstellungen geöffnet", "Emin");
    }

    @FXML
    private void onQuickCreateStudent() {
        setNavSelected(navSchoolButton);
        var firstName = FxUtils.promptText("Neuer Schüler", null, "Vorname:", "");
        if (firstName.isEmpty() || firstName.get().isBlank()) return;
        var lastName = FxUtils.promptText("Neuer Schüler", null, "Nachname:", "");
        if (lastName.isEmpty() || lastName.get().isBlank()) return;
        var className = FxUtils.promptText("Neuer Schüler", null, "Klasse (optional, z.B. 7a):", "");
        try {
            SchoolService ss = new SchoolService();
            Long classId = null;
            if (className.isPresent() && !className.get().isBlank()) {
                classId = ss.ensureClassByName(className.get().trim(), null);
            }
            ss.createStudent(firstName.get().trim(), lastName.get().trim(), classId);
            FxUtils.showInfo("Schüler angelegt", firstName.get().trim() + " " + lastName.get().trim());
            // If school view is active, reload it, otherwise refresh activities
            NavigationManager.showSchoolDatabaseOverview();
            refreshActivities();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Schüler konnte nicht angelegt werden.", e);
        }
    }

    @FXML
    private void onQuickCreateTeacher() {
        setNavSelected(navSchoolButton);
        var firstName = FxUtils.promptText("Neuer Lehrer", null, "Vorname:", "");
        if (firstName.isEmpty() || firstName.get().isBlank()) return;
        var lastName = FxUtils.promptText("Neuer Lehrer", null, "Nachname:", "");
        if (lastName.isEmpty() || lastName.get().isBlank()) return;
        var subject = FxUtils.promptText("Neuer Lehrer", null, "Fach (optional):", "");
        try {
            SchoolService ss = new SchoolService();
            ss.createTeacher(firstName.get().trim(), lastName.get().trim(),
                    subject.isPresent() ? subject.get().trim() : null);
            FxUtils.showInfo("Lehrer angelegt", firstName.get().trim() + " " + lastName.get().trim());
            NavigationManager.showSchoolDatabaseOverview();
            refreshActivities();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Lehrer konnte nicht angelegt werden.", e);
        }
    }

    @FXML
    private void onQuickCreateClass() {
        setNavSelected(navSchoolButton);
        var name = FxUtils.promptText("Neue Klasse", null, "Name (z.B. 7a):", "");
        if (name.isEmpty() || name.get().isBlank()) return;
        var grade = FxUtils.promptInt("Neue Klasse", null, "Jahrgang (optional):", null);
        try {
            SchoolService ss = new SchoolService();
            ss.createClass(name.get().trim(), grade.orElse(null));
            FxUtils.showInfo("Klasse angelegt", name.get().trim());
            NavigationManager.showSchoolDatabaseOverview();
            refreshActivities();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Klasse konnte nicht angelegt werden.", e);
        }
    }

    @FXML
    private void onQuickCreateDevice() {
        setNavSelected(navInventoryButton);
        var tag = FxUtils.promptText("Neues Gerät", null, "Inventar-/Asset-Tag:", "");
        if (tag.isEmpty() || tag.get().isBlank()) return;
        var type = FxUtils.promptText("Neues Gerät", null, "Typ (z.B. iPad, Laptop):", "");
        var status = FxUtils.promptText("Neues Gerät", null, "Status (z.B. Neu, Aktiv):", "Aktiv");
        try {
            InventoryService inv = new InventoryService();
            inv.createDevice(tag.get().trim(), type.orElse(""), status.orElse("Aktiv"));
            FxUtils.showInfo("Gerät angelegt", tag.get().trim());
            refreshActivities();
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Gerät konnte nicht angelegt werden.", e);
        }
    }

    private void addActivity(String area, String action, String user) {
        String time = TIME_FORMATTER.format(LocalDateTime.now());
        activities.add(0, new ActivityEntry(time, area, action, user));
        if (activityTable != null) {
            activityTable.scrollTo(0);
        }
    }
}
