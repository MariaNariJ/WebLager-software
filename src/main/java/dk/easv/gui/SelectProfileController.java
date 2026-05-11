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

    @FXML
    private ComboBox<String> clientComboBox;

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
    public void initialize() {

        clientComboBox.getItems().addAll(
                "SEA - Syddansk Erhvervsakademi"
        );

        clientComboBox.setValue(
                "SEA - Syddansk Erhvervsakademi"
        );

        // Load demo SEA profiles
        loadSEAProfiles();
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
     * Loads demo profiles for SEA client.
     */
    private void loadSEAProfiles() {

        profileContainer.getChildren().clear();

        addProfile(
                "Student Applications",
                "Admission papers and student records"
        );

        addProfile(
                "Exam Archive",
                "Exams and grading documents"
        );

        addProfile(
                "HR & Staff",
                "Employee contracts and HR files"
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
        });

        profileContainer.getChildren().add(card);
    }

    /**
     * Sends selected profile back to
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

        // Send selected profile back
        userviewController.setSelectedProfile(
                selectedProfile
        );

        // Close popup window
        Stage stage = (Stage) continueButton
                .getScene()
                .getWindow();

        stage.close();
    }
}