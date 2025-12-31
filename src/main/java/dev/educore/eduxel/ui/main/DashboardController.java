package dev.educore.eduxel.ui.main;

import dev.educore.eduxel.service.ReportingService;
import dev.educore.eduxel.ui.common.FxUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class DashboardController {

    @FXML private Label studentCountLabel;
    @FXML private Label teacherCountLabel;
    @FXML private Label classCountLabel;
    @FXML private Label deviceCountLabel;
    @FXML private Label sickCountLabel;
    @FXML private Label missingCountLabel;

    @FXML private TableView<ActivityEntry> activityTable;
    @FXML private TableColumn<ActivityEntry, String> timeCol;
    @FXML private TableColumn<ActivityEntry, String> areaCol;
    @FXML private TableColumn<ActivityEntry, String> actionCol;
    @FXML private TableColumn<ActivityEntry, String> userCol;

    private final ReportingService reportingService = new ReportingService();
    private final ObservableList<ActivityEntry> activities = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        refresh();
    }

    private void setupTable() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        activityTable.setItems(activities);
    }

    @FXML
    public void refresh() {
        new Thread(() -> {
            try {
                var stats = reportingService.getGlobalStatistics();
                var latestActivities = reportingService.listRecentActivities(50);

                Platform.runLater(() -> {
                    studentCountLabel.setText(String.valueOf(stats.studentCount));
                    teacherCountLabel.setText(String.valueOf(stats.teacherCount));
                    classCountLabel.setText(String.valueOf(stats.classCount));
                    deviceCountLabel.setText(String.valueOf(stats.deviceCount));
                    sickCountLabel.setText(String.valueOf(stats.sickStudentCount));
                    missingCountLabel.setText(String.valueOf(stats.missingStudentCount));

                    activities.setAll(latestActivities);
                });
            } catch (Exception e) {
                Platform.runLater(() -> FxUtils.showError("Fehler beim Laden des Dashboards", null, e));
            }
        }).start();
    }
}
