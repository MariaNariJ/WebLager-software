package dk.easv.gui;

import dk.easv.be.Log;
import dk.easv.bll.LogManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.function.Function;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;


public class AdminLogsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;

    @FXML private TableView<Log> scanningTable;
    @FXML private TableView<Log> exportTable;
    @FXML private TableView<Log> userTable;

    @FXML private Label scanningCountLabel;
    @FXML private Label exportCountLabel;
    @FXML private Label userCountLabel;

    private final LogManager logManager = new LogManager();

    private ObservableList<Log> allScanningLogs;
    private ObservableList<Log> allExportLogs;
    private ObservableList<Log> allUserLogs;

    @FXML
    private void initialize() {
        setupFilters();

        setupTable(scanningTable);
        setupTable(exportTable);
        setupTable(userTable);

        loadLogs();
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void setupFilters() {
        typeFilter.getItems().addAll("All Types", "Scanning", "Export", "User");
        typeFilter.setValue("All Types");
    }

    private void loadLogs() {
        allScanningLogs = FXCollections.observableArrayList(logManager.getLogsByType("Scanning"));
        allExportLogs = FXCollections.observableArrayList(logManager.getLogsByType("Export"));
        allUserLogs = FXCollections.observableArrayList(logManager.getLogsByType("User"));

        applyFilters();
    }

    private void setupTable(TableView<Log> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setFixedCellSize(24);

        table.getColumns().clear();

        table.getColumns().add(createColumn("Timestamp", log -> String.valueOf(log.getTimestamp())));
        table.getColumns().add(createColumn("Event", Log::getEvent));
        table.getColumns().add(createColumn("User", Log::getUsername));
        table.getColumns().add(createColumn("Details", Log::getDetails));
        table.getColumns().add(createColumn("Status", Log::getStatus));
    }

    private TableColumn<Log, String> createColumn(String title, Function<Log, String> getter) {
        TableColumn<Log, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new SimpleStringProperty(getter.apply(data.getValue()))
        );
        return column;
    }

    private void applyFilters() {
        String search = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String selectedType = typeFilter.getValue();

        boolean showScanning = selectedType.equals("All Types") || selectedType.equals("Scanning");
        boolean showExport = selectedType.equals("All Types") || selectedType.equals("Export");
        boolean showUser = selectedType.equals("All Types") || selectedType.equals("User");

        scanningTable.setItems(filterLogs(allScanningLogs, search));
        exportTable.setItems(filterLogs(allExportLogs, search));
        userTable.setItems(filterLogs(allUserLogs, search));

        scanningTable.setVisible(showScanning);
        scanningTable.setManaged(showScanning);
        scanningTable.getParent().setVisible(showScanning);
        scanningTable.getParent().setManaged(showScanning);

        exportTable.setVisible(showExport);
        exportTable.setManaged(showExport);
        exportTable.getParent().setVisible(showExport);
        exportTable.getParent().setManaged(showExport);

        userTable.setVisible(showUser);
        userTable.setManaged(showUser);
        userTable.getParent().setVisible(showUser);
        userTable.getParent().setManaged(showUser);

        scanningCountLabel.setText(String.valueOf(scanningTable.getItems().size()));
        exportCountLabel.setText(String.valueOf(exportTable.getItems().size()));
        userCountLabel.setText(String.valueOf(userTable.getItems().size()));
    }

    private ObservableList<Log> filterLogs(ObservableList<Log> source, String search) {
        List<Log> filtered = new ArrayList<>();

        for (Log log : source) {
            boolean matchesSearch =
                    search.isEmpty()
                            || safe(log.getEvent()).toLowerCase().contains(search)
                            || safe(log.getUsername()).toLowerCase().contains(search)
                            || safe(log.getDetails()).toLowerCase().contains(search)
                            || safe(log.getStatus()).toLowerCase().contains(search)
                            || String.valueOf(log.getTimestamp()).toLowerCase().contains(search);

            if (matchesSearch) {
                filtered.add(log);
            }
        }

        return FXCollections.observableArrayList(filtered);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}