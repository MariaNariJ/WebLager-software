package dk.easv.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminLogsController {

    @FXML private TextField searchField;
    @FXML private TextField dateField;

    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> typeFilter;

    @FXML private TableView<String[]> scanningTable;
    @FXML private TableView<String[]> qaTable;
    @FXML private TableView<String[]> exportTable;
    @FXML private TableView<String[]> userTable;

    @FXML
    private void initialize() {
        setupFilters();

        setupTable(scanningTable);
        setupTable(qaTable);
        setupTable(exportTable);
        setupTable(userTable);

        scanningTable.setItems(FXCollections.observableArrayList(
                new String[]{"May 25, 2026 18:45:32", "Info", "Document Scanned", "rocky", "Scanned document \"Contract.pdf\" (45 pages)", "Completed", "00:02:34"},
                new String[]{"May 25, 2026 18:42:15", "Warning", "Scan Quality Issue", "valeri", "Low resolution detected on page 12", "Warning", "00:01:12"},
                new String[]{"May 25, 2026 18:40:03", "Info", "Batch Scan Completed", "jakub", "Batch scan completed (12 documents)", "Completed", "00:08:45"},
                new String[]{"May 25, 2026 18:35:47", "Error", "Scanner Connection Lost", "system", "Connection lost with scanner", "Failed", "00:00:00"}
        ));

        qaTable.setItems(FXCollections.observableArrayList(
                new String[]{"May 25, 2026 18:44:21", "Info", "QA Review Completed", "valeri", "QA review completed for document \"Report_2025.pdf\"", "Approved", "00:05:21"},
                new String[]{"May 25, 2026 18:39:10", "Warning", "QA Issue Found", "rocky", "Missing metadata: Document Type", "Needs Attention", "00:03:15"},
                new String[]{"May 25, 2026 18:32:55", "Info", "QA Started", "jakub", "QA review started for document \"Invoice_1024.pdf\"", "In Progress", "00:00:45"},
                new String[]{"May 25, 2026 18:28:11", "Error", "QA Failed", "system", "QA validation failed: Corrupted file", "Failed", "00:00:00"}
        ));

        exportTable.setItems(FXCollections.observableArrayList(
                new String[]{"May 25, 2026 18:43:05", "Info", "Export Completed", "rocky", "Exported 5 documents to C:\\Exports\\May25", "Completed", "00:01:48"},
                new String[]{"May 25, 2026 18:37:22", "Warning", "Export Partial", "valeri", "3 of 5 documents exported (2 failed)", "Partial", "00:02:11"},
                new String[]{"May 25, 2026 18:30:18", "Error", "Export Failed", "admin", "Export failed: Insufficient disk space", "Failed", "00:00:00"},
                new String[]{"May 25, 2026 18:25:36", "Info", "Export Started", "jakub", "Export started: 12 documents selected", "In Progress", "00:00:05"}
        ));

        userTable.setItems(FXCollections.observableArrayList(
                new String[]{"May 25, 2026 18:41:12", "Info", "User Created", "admin", "Created new user: maria", "Completed", "00:00:03"},
                new String[]{"May 25, 2026 18:15:09", "Warning", "User Updated", "jakub", "Changed role for user: valeri", "Warning", "00:00:01"},
                new String[]{"May 25, 2026 18:05:44", "Info", "User Deactivated", "admin", "Deactivated user: rocky", "Completed", "00:00:02"},
                new String[]{"May 25, 2026 17:59:18", "Error", "User Delete Failed", "system", "Could not delete user because of database relation", "Failed", "00:00:00"}
        ));
    }

    private void setupFilters() {
        levelFilter.getItems().addAll("All Levels", "Info", "Warning", "Error");
        levelFilter.setValue("All Levels");

        typeFilter.getItems().addAll("All Types", "Scanning", "QA", "Export", "User");
        typeFilter.setValue("All Types");
    }

    private void setupTable(TableView<String[]> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setFixedCellSize(24);

        table.getColumns().add(createColumn("Timestamp", 0));
        table.getColumns().add(createColumn("Level", 1));
        table.getColumns().add(createColumn("Event", 2));
        table.getColumns().add(createColumn("User", 3));
        table.getColumns().add(createColumn("Details", 4));
        table.getColumns().add(createColumn("Status", 5));
        table.getColumns().add(createColumn("Duration", 6));
    }

    private TableColumn<String[], String> createColumn(String title, int index) {
        TableColumn<String[], String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[index])
        );
        return column;
    }
}