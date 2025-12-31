package dev.educore.eduxel.ui.main;

import dev.educore.eduxel.meta.EduxelMeta;
import dev.educore.eduxel.config.ClientConfig;
import dev.educore.eduxel.persistence.SchemaBootstrapper;
import dev.educore.eduxel.navigation.NavigationManager;
import dev.educore.eduxel.service.ActivityLogger;
import dev.educore.eduxel.service.ReportingService;
import dev.educore.eduxel.service.SchoolService;
import dev.educore.eduxel.service.InventoryService;
import dev.educore.eduxel.ui.common.FxUtils;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.ui.school.StudentDetailController;
import dev.educore.eduxel.ui.school.TeacherDetailController;
import dev.educore.eduxel.ui.school.ClassDetailController;
import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.persistence.school.TeacherRepository;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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
    private VBox navDashboardButton;

    @FXML
    private VBox navSchoolButton;

    @FXML
    private VBox navInventoryButton;

    @FXML
    private VBox navSearchButton;

    @FXML
    private VBox navSettingsButton;

    @FXML
    private VBox navWebDashboardButton;

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

        // add smooth hover/press animation to all nav items we have

        // Dashboard will be shown in onAppReady() to ensure DB is bootstrapped first
    }

    private void addNavHoverEffect(VBox box) {
        if (box == null) return;
        // Use Timeline for smooth transitions on hover and press
        final DropShadow hoverShadow = new DropShadow(8, Color.rgb(0,0,0,0.08));
        hoverShadow.setOffsetY(1);

        final Timeline enter = new Timeline(
                new KeyFrame(Duration.millis(180),
                        new KeyValue(box.scaleXProperty(), 1.01),
                        new KeyValue(box.scaleYProperty(), 1.01)
                )
        );
        final Timeline exit = new Timeline(
                new KeyFrame(Duration.millis(180),
                        new KeyValue(box.scaleXProperty(), 1.0),
                        new KeyValue(box.scaleYProperty(), 1.0)
                )
        );

        box.setOnMouseEntered(e -> {
            enter.playFromStart();
            box.setEffect(hoverShadow);
        });
        box.setOnMouseExited(e -> {
            exit.playFromStart();
            box.setEffect(null);
        });
        box.setOnMousePressed(e -> box.setScaleX(0.995));
        box.setOnMouseReleased(e -> box.setScaleX(1.01));
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
        if (navDashboardButton != null) {
            navDashboardButton.getStyleClass().remove("nav-item-selected");
        }
        if (navSchoolButton != null) {
            navSchoolButton.getStyleClass().remove("nav-item-selected");
        }
        if (navInventoryButton != null) {
            navInventoryButton.getStyleClass().remove("nav-item-selected");
        }
        if (navSearchButton != null) {
            navSearchButton.getStyleClass().remove("nav-item-selected");
        }
        if (navSettingsButton != null) {
            navSettingsButton.getStyleClass().remove("nav-item-selected");
        }
        if (navWebDashboardButton != null) {
            navWebDashboardButton.getStyleClass().remove("nav-item-selected");
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
            // refreshActivities(); // Not needed here anymore, Dashboard handles it
            ActivityLogger.log("System", "App gestartet", EduxelMeta.APP_NAME);
            NavigationManager.showDashboard();
            setNavSelected(navDashboardButton);
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
        // Handled by Dashboard now
    }

    private boolean checkConnection() {
        try {
            dev.educore.eduxel.persistence.DataSourceProvider.getConnection().close();
            return true;
        } catch (Exception e) {
            FxUtils.showError("Keine Verbindung",
                    "Es konnte keine Verbindung zum Eduxel-Server hergestellt werden.\n" +
                    "Bitte prüfen Sie die Einstellungen.", e);
            onNavSettingsClicked();
            return false;
        }
    }

    @FXML
    private void onNavDashboardClicked() {
        if (!checkConnection()) return;
        setNavSelected(navDashboardButton);
        NavigationManager.showDashboard();
    }

    @FXML
    private void onNavSchoolClicked() {
        if (!checkConnection()) return;
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
        if (!checkConnection()) return;
        setNavSelected(navInventoryButton);
        NavigationManager.showInventoryOverview();
        addActivity("Inventar", "Inventarübersicht geöffnet", "Emin");
    }

    @FXML
    private void onNavSearchClicked() {
        if (!checkConnection()) return;
        setNavSelected(navSearchButton);
        NavigationManager.showSearch();
        addActivity("Suche", "Globale Suche geöffnet", "Emin");
    }

    @FXML
    private void onNavWebDashboardClicked() {
        if (!checkConnection()) return;
        setNavSelected(navWebDashboardButton);
        NavigationManager.showWebDashboard();
        addActivity("Web", "Web Dashboard geöffnet", "Emin");
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
        try {
            // Schulansicht anzeigen, damit das Kontextgefühl stimmt
            NavigationManager.showSchoolDatabaseOverview();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/student-detail.fxml"));
            DialogPane pane = loader.load();
            StudentDetailController controller = loader.getController();
            controller.initForCreate();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neuen Schüler anlegen");
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Student created = controller.buildResult();
                SchoolService ss = new SchoolService();
                ss.createStudent(created);
                FxUtils.showInfo("Schüler angelegt", created.getFirstName() + " " + created.getLastName());
                refreshActivities();
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Schüler konnte nicht angelegt werden.", e);
        }
    }

    @FXML
    private void onQuickCreateTeacher() {
        setNavSelected(navSchoolButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/teacher-detail.fxml"));
            DialogPane pane = loader.load();
            TeacherDetailController controller = loader.getController();
            controller.initForCreate();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neuen Lehrer anlegen");
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;
                SchoolService ss = new SchoolService();
                ss.createTeacher(controller.getFirstName(), controller.getLastName(), controller.getSubject());
                FxUtils.showInfo("Lehrer angelegt", controller.getFirstName() + " " + controller.getLastName());
                NavigationManager.showSchoolDatabaseOverview();
                refreshActivities();
            }
        } catch (Exception e) {
            FxUtils.showError("Fehler", "Lehrer konnte nicht angelegt werden.", e);
        }
    }

    @FXML
    private void onQuickCreateClass() {
        setNavSelected(navSchoolButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/educore/eduxel/ui/school/class-detail.fxml"));
            DialogPane pane = loader.load();
            ClassDetailController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Neue Klasse anlegen");
            FxUtils.applyCustomStyle(dialog);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validate()) return;

                ClassGroup group = new ClassGroup();
                group.setName(controller.getName());
                group.setGrade(controller.getGrade());
                group.setSchoolType(controller.getSchoolType());
                group.setRoom(controller.getRoom());
                TeacherRepository.TeacherItem teacherItem = controller.getSelectedTeacher();
                if (teacherItem != null) {
                    group.setTeacherId(teacherItem.id);
                }

                SchoolService ss = new SchoolService();
                ss.createClass(group);
                FxUtils.showInfo("Klasse angelegt", group.getName());
                NavigationManager.showSchoolDatabaseOverview();
                refreshActivities();
            }
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
