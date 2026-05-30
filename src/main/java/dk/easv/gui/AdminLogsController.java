package dk.easv.gui;

import dk.easv.be.Log;
import dk.easv.bll.LogManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.function.Function;


public class AdminLogsController {

    @FXML private TextField searchField;
    @FXML private TextField dateField;

    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> typeFilter;

    @FXML private TableView<Log> scanningTable;
    @FXML private TableView<Log> exportTable;
    @FXML private TableView<Log> userTable;

    @FXML private Label scanningCountLabel;
    @FXML private Label exportCountLabel;
    @FXML private Label userCountLabel;

    private final LogManager logManager = new LogManager();

    @FXML
    private void initialize() {
        setupFilters();

        setupTable(scanningTable);
        setupTable(exportTable);
        setupTable(userTable);

        loadLogs();
    }

    private void setupFilters() {
        levelFilter.getItems().addAll("All Levels", "Info", "Warning", "Error");
        levelFilter.setValue("All Levels");

        typeFilter.getItems().addAll("All Types", "Scanning", "Export", "User");
        typeFilter.setValue("All Types");
    }

    private void loadLogs() {
        var scanningLogs = FXCollections.observableArrayList(logManager.getLogsByType("Scanning"));
        var exportLogs = FXCollections.observableArrayList(logManager.getLogsByType("Export"));
        var userLogs = FXCollections.observableArrayList(logManager.getLogsByType("User"));

        scanningTable.setItems(scanningLogs);
        exportTable.setItems(exportLogs);
        userTable.setItems(userLogs);

        scanningCountLabel.setText(String.valueOf(scanningLogs.size()));
        exportCountLabel.setText(String.valueOf(exportLogs.size()));
        userCountLabel.setText(String.valueOf(userLogs.size()));
    }

    private void setupTable(TableView<Log> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setFixedCellSize(24);

        table.getColumns().clear();

        table.getColumns().add(createColumn("Timestamp", log -> String.valueOf(log.getTimestamp())));
        table.getColumns().add(createColumn("Level", Log::getLevel));
        table.getColumns().add(createColumn("Event", Log::getEvent));
        table.getColumns().add(createColumn("User", Log::getUsername));
        table.getColumns().add(createColumn("Details", Log::getDetails));
        table.getColumns().add(createColumn("Status", Log::getStatus));
        table.getColumns().add(createColumn("Duration", Log::getDuration));
    }

    private TableColumn<Log, String> createColumn(String title, Function<Log, String> getter) {
        TableColumn<Log, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new SimpleStringProperty(getter.apply(data.getValue()))
        );
        return column;
    }
}