package dk.easv.gui;

import dk.easv.be.Box;
import dk.easv.bll.ExportManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class UserExportController {

    @FXML private ComboBox<Box> boxComboBox;
    @FXML private ComboBox<String> exportFormatComboBox;
    @FXML private TextField destinationField;
    @FXML private Button browseButton;
    @FXML private Button exportButton;

    private final ExportManager exportManager = new ExportManager();
    private final dk.easv.bll.DocumentManager documentManager = new dk.easv.bll.DocumentManager();

    @FXML
    private void initialize() {
        setupExportFormat();
        setupBoxes();

        browseButton.setOnAction(e -> chooseFolder());
        exportButton.setOnAction(e -> exportFiles());
    }

    private void setupExportFormat() {
        exportFormatComboBox.setItems(FXCollections.observableArrayList(
                "Single-page TIFF",
                "Multi-page TIFF"
        ));

        exportFormatComboBox.setValue("Single-page TIFF");
    }

    private void setupBoxes() {
        boxComboBox.setItems(FXCollections.observableArrayList(
                documentManager.getBoxesForExport()
        ));

        boxComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Box box, boolean empty) {
                super.updateItem(box, empty);
                setText(empty || box == null ? null : box.getBoxName());
            }
        });

        boxComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Box box, boolean empty) {
                super.updateItem(box, empty);
                setText(empty || box == null ? null : box.getBoxName());
            }
        });

        if (!boxComboBox.getItems().isEmpty()) {
            boxComboBox.setValue(boxComboBox.getItems().getFirst());
        }
    }

    private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose export folder");

        File selectedFolder = chooser.showDialog(destinationField.getScene().getWindow());

        if (selectedFolder != null) {
            destinationField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void exportFiles() {
        Box selectedBox = boxComboBox.getValue();

        if (selectedBox == null) {
            showAlert("Please select a box.");
            return;
        }

        if (destinationField.getText() == null || destinationField.getText().isBlank()) {
            showAlert("Please choose an export folder.");
            return;
        }

        File folder = new File(destinationField.getText());

        if (!folder.exists() || !folder.isDirectory()) {
            showAlert("The selected export folder does not exist.");
            return;
        }

        try {
            if ("Single-page TIFF".equals(exportFormatComboBox.getValue())) {
                exportManager.exportSinglePageTiffs(selectedBox.getId(), folder);
            }

            showAlert("Export completed.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export failed: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}