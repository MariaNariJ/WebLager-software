package dk.easv.gui;

import dk.easv.be.Box;
import dk.easv.be.Document;
import dk.easv.bll.DocumentManager;
import dk.easv.bll.ExportManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.VBox;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.Desktop;
import dk.easv.bll.LogManager;
import dk.easv.be.User;

public class UserExportController {

    @FXML private ComboBox<Box> boxComboBox;
    @FXML private ComboBox<String> exportFormatComboBox;
    @FXML private TextField destinationField;
    @FXML private Button browseButton;
    @FXML private Button exportButton;

    @FXML private RadioButton selectedDocumentRadio;
    @FXML private RadioButton multipleDocumentsRadio;
    @FXML private RadioButton allDocumentsRadio;
    @FXML private Button selectDocumentsButton;

    @FXML private Label multipleSelectedCountLabel;
    @FXML private Label exportStatusLabel;
    @FXML private Label exportDocumentCountLabel;
    @FXML private Label exportFormatStatusLabel;
    @FXML private CheckBox openFolderCheckBox;
    @FXML private ComboBox<Document> selectedDocumentComboBox;
    @FXML private Label allDocumentsCountLabel;

    private final ExportManager exportManager = new ExportManager();
    private final DocumentManager documentManager = new DocumentManager();

    private final ToggleGroup exportScopeGroup = new ToggleGroup();

    private List<Document> documentsInSelectedBox = new ArrayList<>();
    private final List<Document> selectedDocuments = new ArrayList<>();

    private final LogManager logManager = new LogManager();
    private User loggedInUser;

    @FXML
    private void initialize() {
        setupRadioButtons();
        setupExportFormat();
        setupSelectedDocumentComboBox();
        setupBoxes();

        browseButton.setOnAction(e -> chooseFolder());
        exportButton.setOnAction(e -> exportFiles());
        selectDocumentsButton.setOnAction(e -> showDocumentSelectionDialog());

        boxComboBox.setOnAction(e -> loadDocumentsForSelectedBox());
        exportFormatComboBox.setOnAction(e -> updateExportStatus());

        updateExportStatus();
    }

    private void setupRadioButtons() {
        selectedDocumentRadio.setToggleGroup(exportScopeGroup);
        multipleDocumentsRadio.setToggleGroup(exportScopeGroup);
        allDocumentsRadio.setToggleGroup(exportScopeGroup);

        allDocumentsRadio.setSelected(true);

        exportScopeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            selectDocumentsButton.setDisable(!multipleDocumentsRadio.isSelected());
            updateExportStatus();
        });

        selectDocumentsButton.setDisable(true);
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
            loadDocumentsForSelectedBox();
        }
    }

    private void loadDocumentsForSelectedBox() {
        Box selectedBox = boxComboBox.getValue();

        selectedDocuments.clear();

        if (selectedBox == null) {
            documentsInSelectedBox = new ArrayList<>();
        } else {
            documentsInSelectedBox = documentManager.getDocumentsForBox(selectedBox.getId());
        }

        selectedDocumentComboBox.setItems(FXCollections.observableArrayList(documentsInSelectedBox));

        if (!documentsInSelectedBox.isEmpty()) {
            selectedDocumentComboBox.setValue(documentsInSelectedBox.getFirst());
        } else {
            selectedDocumentComboBox.setValue(null);
        }

        updateExportStatus();
    }

    private void showDocumentSelectionDialog() {
        if (documentsInSelectedBox.isEmpty()) {
            showAlert("There are no documents in this box.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Documents");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/user-export.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("export-dialog");
        dialogPane.setPrefWidth(360);

        VBox content = new VBox(8);
        content.getStyleClass().add("export-dialog-content");

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (Document document : documentsInSelectedBox) {
            CheckBox checkBox = new CheckBox(document.getDocumentName());
            checkBox.getStyleClass().add("export-dialog-check");
            checkBox.setUserData(document);

            if (selectedDocuments.contains(document)) {
                checkBox.setSelected(true);
            }

            checkBoxes.add(checkBox);
            content.getChildren().add(checkBox);
        }

        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("export-button");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("export-secondary-button");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                selectedDocuments.clear();

                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isSelected()) {
                        selectedDocuments.add((Document) checkBox.getUserData());
                    }
                }

                updateExportStatus();
            }
        });
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
        File folder = getDestinationFolder();

        if (folder == null) {
            return;
        }

        List<Document> documentsToExport = getDocumentsToExport();

        if (documentsToExport.isEmpty()) {
            showAlert("No documents selected for export.");
            return;
        }

        try {
            int exportedCount;

            if ("Single-page TIFF".equals(exportFormatComboBox.getValue())) {
                exportedCount = exportManager.exportSinglePageTiffs(documentsToExport, folder);
            } else {
                exportedCount = exportManager.exportMultiPageTiffs(documentsToExport, folder);
            }

            showAlert("Export completed. Exported " + exportedCount + " TIFF file(s).");

            logManager.createLog(
                    "Export",
                    "Export Completed",
                    getLoggedInUserId(),
                    "Exported " + exportedCount + " TIFF file(s) to " + folder.getAbsolutePath(),
                    "Completed"
            );

            if (openFolderCheckBox.isSelected()) {
                openFolder(folder);
            }

            updateExportStatus();



        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export failed: " + e.getMessage());
            logManager.createLog(
                    "Export",
                    "Export Failed",
                    getLoggedInUserId(),
                    e.getMessage(),
                    "Failed"
            );
        }
    }

    private File getDestinationFolder() {
        if (destinationField.getText() == null || destinationField.getText().isBlank()) {
            showAlert("Please choose an export folder.");
            return null;
        }

        File folder = new File(destinationField.getText());

        if (!folder.exists() || !folder.isDirectory()) {
            showAlert("The selected export folder does not exist.");
            return null;
        }

        return folder;
    }

    private List<Document> getDocumentsToExport() {
        if (selectedDocumentRadio.isSelected()) {
            Document selectedDocument = selectedDocumentComboBox.getValue();

            if (selectedDocument == null) {
                return new ArrayList<>();
            }

            return List.of(selectedDocument);
        }

        if (multipleDocumentsRadio.isSelected()) {
            return new ArrayList<>(selectedDocuments);
        }

        return new ArrayList<>(documentsInSelectedBox);
    }

    private void updateExportStatus() {
        int count = getDocumentsToExport().size();
        allDocumentsCountLabel.setText(documentsInSelectedBox.size() + " documents");

        multipleSelectedCountLabel.setText(selectedDocuments.size() + " selected");
        exportDocumentCountLabel.setText(count + " selected");

        if ("Single-page TIFF".equals(exportFormatComboBox.getValue())) {
            exportFormatStatusLabel.setText("Single-page TIFF");
        } else {
            exportFormatStatusLabel.setText("Multi-page TIFF");
        }

        if (count == 0) {
            exportStatusLabel.setText("No documents selected");
        } else {
            exportStatusLabel.setText("Ready to export documents");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/user-export.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("export-dialog");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("export-button");

        alert.showAndWait();
    }

    private void setupSelectedDocumentComboBox() {
        selectedDocumentComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Document document, boolean empty) {
                super.updateItem(document, empty);
                setText(empty || document == null ? null : document.getDocumentName());
            }
        });

        selectedDocumentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Document document, boolean empty) {
                super.updateItem(document, empty);
                setText(empty || document == null ? null : document.getDocumentName());
            }
        });

        selectedDocumentComboBox.setOnAction(e -> updateExportStatus());
    }

    private void openFolder(File folder) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
            }
        } catch (Exception e) {
            showAlert("Export finished, but the folder could not be opened.");
        }
    }
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    private Integer getLoggedInUserId() {
        return loggedInUser != null ? loggedInUser.getId() : null;
    }
}