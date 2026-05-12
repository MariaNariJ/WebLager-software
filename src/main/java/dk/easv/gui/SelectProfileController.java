package dk.easv.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SelectProfileController {

    // Container holding all profile cards
    @FXML
    private VBox profileContainer;

    // Client dropdown
    @FXML
    private ComboBox<String> clientComboBox;

    // Box selection dropdown
    @FXML
    private ComboBox<String> boxComboBox;

    // Continue button from popup
    @FXML
    private Button continueButton;

    // Currently selected profile card
    private VBox selectedCard;

    // Stores selected profile name
    private String selectedProfile;

    // Reference to main controller
    private UserviewController userviewController;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {

        // Demo client
        clientComboBox.getItems().addAll(
                "SEA - Syddansk Erhvervsakademi"
        );

        clientComboBox.setValue(
                "SEA - Syddansk Erhvervsakademi"
        );

        // Demo box IDs for testing scan sessions
        boxComboBox.getItems().addAll(
                "BOX_001",
                "BOX_002",
                "BOX_003"
        );

        // Default selected box
        boxComboBox.setValue("BOX_001");

        // Load demo scan profiles
        loadSEAProfiles();

        // Continue button starts disabled until a profile is selected
        continueButton.setDisable(true);
    }

    /**
     * Allows popup controller to communicate
     * with UserviewController.
     */
    public void setUserviewController(
            UserviewController controller) {

        this.userviewController = controller;
    }

    /**
     * Loads demo scan profiles.
     */
    private void loadSEAProfiles() {

        profileContainer.getChildren().clear();

        addProfile(
                "Auto Rotate",
                "Automatically straightens scanned pages"
        );

        addProfile(
                "High Brightness",
                "Brightens dark scanned documents"
        );

        addProfile(
                "Archive Quality",
                "High quality settings for long-term storage"
        );

        addProfile(
                "Fast Scan",
                "Optimized for speed and reduced file size"
        );
    }

    /**
     * Creates a clickable profile card.
     */
    private void addProfile(
            String title,
            String description) {

        VBox card = new VBox(4);

        card.getStyleClass().add("profile-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("profile-title");

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.getStyleClass()
                .add("profile-description");

        card.getChildren().addAll(
                titleLabel,
                descriptionLabel
        );

        // Handle profile selection
        card.setOnMouseClicked(event -> {

            // Remove old selection
            if (selectedCard != null) {
                selectedCard.getStyleClass()
                        .remove("profile-card-selected");
            }

            // Save selected card
            selectedCard = card;

            // Highlight selected card
            if (!card.getStyleClass()
                    .contains("profile-card-selected")) {

                card.getStyleClass()
                        .add("profile-card-selected");
            }

            // Save selected profile name
            selectedProfile = title;

            // Enable continue button after selection
            continueButton.setDisable(false);
        });

        profileContainer.getChildren().add(card);
    }

    /**
     * Sends selected scan setup back to
     * UserviewController and closes popup.
     */
    @FXML
    private void onContinueClicked() {

        // Prevent continue without selection
        if (selectedProfile == null) {
            return;
        }

        // Send selected client back
        userviewController.setSelectedClient(
                clientComboBox.getValue()
        );

        // Send selected scan setup back
        userviewController.setScanSetup(
                boxComboBox.getValue(),
                selectedProfile
        );

        // Close popup window
        Stage stage = (Stage) continueButton
                .getScene()
                .getWindow();

        stage.close();
    }
    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}