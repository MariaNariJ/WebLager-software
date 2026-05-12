package dk.easv.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SaveDocumentController {

    @FXML
    private TextField txtDocumentName;

    private UserviewController userviewController;

    public void setUserviewController(UserviewController controller) {
        this.userviewController = controller;
    }

    @FXML
    private void onSaveClicked() {

        String documentName = txtDocumentName.getText();

        // Default name if empty
        if (documentName == null || documentName.isBlank()) {

            documentName = "Document";
        }

        // Send chosen name back to UserviewController
        userviewController.saveCurrentDocument(documentName);

        // Close popup
        Stage stage = (Stage) txtDocumentName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancelClicked() {

        closeWindow();
    }

    private void closeWindow() {

        Stage stage =
                (Stage) txtDocumentName
                        .getScene()
                        .getWindow();

        stage.close();
    }


}