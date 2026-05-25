package dk.easv.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminLogsController {

    @FXML
    private VBox logsRoot;

    @FXML
    private void initialize() {

        logsRoot.setSpacing(10);
        logsRoot.setFillWidth(true);

        HBox filterBar = createFilterBar();

        VBox scanningLogs = createLogSection(
                "Scanning Logs",
                "Logs related to document scanning activities.",
                "1,245",
                new String[][]{
                        {"May 25, 2026 18:45:32", "Info", "Document Scanned", "rocky", "Scanned document \"Contract.pdf\" (45 pages)", "Completed", "00:02:34"},
                        {"May 25, 2026 18:42:15", "Warning", "Scan Quality Issue", "valeri", "Low resolution detected on page 12", "Warning", "00:01:12"},
                        {"May 25, 2026 18:40:03", "Info", "Batch Scan Completed", "jakub", "Batch scan completed (12 documents)", "Completed", "00:08:45"},
                        {"May 25, 2026 18:35:47", "Error", "Scanner Connection Lost", "system", "Connection lost with scanner", "Failed", "00:00:00"}
                }
        );

        VBox qaLogs = createLogSection(
                "QA Logs",
                "Logs related to quality assurance activities.",
                "876",
                new String[][]{
                        {"May 25, 2026 18:44:21", "Info", "QA Review Completed", "valeri", "QA review completed for document \"Report_2025.pdf\"", "Approved", "00:05:21"},
                        {"May 25, 2026 18:39:10", "Warning", "QA Issue Found", "rocky", "Missing metadata: Document Type", "Needs Attention", "00:03:15"},
                        {"May 25, 2026 18:32:55", "Info", "QA Started", "jakub", "QA review started for document \"Invoice_1024.pdf\"", "In Progress", "00:00:45"},
                        {"May 25, 2026 18:28:11", "Error", "QA Failed", "system", "QA validation failed: Corrupted file", "Failed", "00:00:00"}
                }
        );

        VBox exportLogs = createLogSection(
                "Export Logs",
                "Logs related to document export activities.",
                "564",
                new String[][]{
                        {"May 25, 2026 18:43:05", "Info", "Export Completed", "rocky", "Exported 5 documents to C:\\Exports\\May25", "Completed", "00:01:48"},
                        {"May 25, 2026 18:37:22", "Warning", "Export Partial", "valeri", "3 of 5 documents exported (2 failed)", "Partial", "00:02:11"},
                        {"May 25, 2026 18:30:18", "Error", "Export Failed", "admin", "Export failed: Insufficient disk space", "Failed", "00:00:00"},
                        {"May 25, 2026 18:25:36", "Info", "Export Started", "jakub", "Export started: 12 documents selected", "In Progress", "00:00:05"}
                }
        );

        VBox userLogs = createLogSection(
                "User Logs",
                "Logs related to user account changes.",
                "1,032",
                new String[][]{
                        {"May 25, 2026 18:41:12", "Info", "User Created", "admin", "Created new user: maria", "Completed", "00:00:03"},
                        {"May 25, 2026 18:15:09", "Warning", "User Updated", "jakub", "Changed role for user: valeri", "Warning", "00:00:01"},
                        {"May 25, 2026 18:05:44", "Info", "User Deactivated", "admin", "Deactivated user: rocky", "Completed", "00:00:02"},
                        {"May 25, 2026 17:59:18", "Error", "User Delete Failed", "system", "Could not delete user because of database relation", "Failed", "00:00:00"}
                }
        );

        logsRoot.getChildren().addAll(filterBar, scanningLogs, qaLogs, exportLogs, userLogs);

        VBox.setVgrow(scanningLogs, Priority.ALWAYS);
        VBox.setVgrow(qaLogs, Priority.ALWAYS);
        VBox.setVgrow(exportLogs, Priority.ALWAYS);
    }

    private HBox createFilterBar() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.getStyleClass().add("admin-input");
        searchField.setPrefWidth(260);

        ComboBox<String> levelBox = new ComboBox<>();
        levelBox.getItems().addAll("All Levels", "Info", "Warning", "Error");
        levelBox.setValue("All Levels");
        levelBox.getStyleClass().add("admin-combo");
        levelBox.setPrefWidth(140);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("All Types", "Scanning", "QA", "Export");
        typeBox.setValue("All Types");
        typeBox.getStyleClass().add("admin-combo");
        typeBox.setPrefWidth(140);

        TextField dateField = new TextField("May 18, 2026 - May 25, 2026");
        dateField.getStyleClass().add("admin-input");
        dateField.setPrefWidth(230);

        Button filterButton = new Button("Filters");
        filterButton.getStyleClass().add("admin-secondary-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(10, searchField, levelBox, typeBox, dateField, spacer, filterButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("logs-filter-bar");

        return bar;
    }

    private VBox createLogSection(String title, String description, String count, String[][] rows) {

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("logs-section-title");

        Label countLabel = new Label(count);
        countLabel.getStyleClass().add("logs-count-pill");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("logs-description");

        Button viewAllButton = new Button("View All");
        viewAllButton.getStyleClass().add("logs-link-button");

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleRow = new HBox(10, titleLabel, countLabel, titleSpacer, viewAllButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        TableView<String[]> table = new TableView<>();
        table.getStyleClass().add("logs-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setFixedCellSize(24);
        table.setPrefHeight(190);
        table.setMinHeight(190);

        table.getColumns().add(createColumn("Timestamp", 0));
        table.getColumns().add(createColumn("Level", 1));
        table.getColumns().add(createColumn("Event", 2));
        table.getColumns().add(createColumn("User", 3));
        table.getColumns().add(createColumn("Details", 4));
        table.getColumns().add(createColumn("Status", 5));
        table.getColumns().add(createColumn("Duration", 6));

        table.getItems().addAll(rows);

        VBox section = new VBox(8, titleRow, descriptionLabel, table);

        section.getStyleClass().add("logs-section");
        VBox.setVgrow(table, Priority.ALWAYS);

        return section;
    }

    private TableColumn<String[], String> createColumn(String title, int index) {
        TableColumn<String[], String> column = new TableColumn<>(title);
        column.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue()[index])
        );
        return column;
    }
}