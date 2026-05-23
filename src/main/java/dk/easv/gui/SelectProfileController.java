package dk.easv.gui;

import dk.easv.bll.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dk.easv.be.Profile;
import java.util.ArrayList;
import java.util.List;

public class SelectProfileController {

    @FXML
    private VBox profileContainer;

    @FXML
    private ComboBox<String> clientComboBox;

    @FXML
    private javafx.scene.control.TextField txtBox;

    @FXML
    private Button continueButton;

    @FXML
    private Button cancelButton;

    private VBox selectedCard;

    private String selectedProfile;
    private final List<Profile> currentProfiles =
            new ArrayList<>();

    @FXML
    private TextField txtProfileSearch;

    private UserviewController userviewController;

    private final ClientManager clientManager =
            new ClientManager();

    @FXML
    public void initialize() {

        txtBox.setText("BOX_001");

        loadSEAProfiles();

        txtProfileSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    filterProfiles(newValue);
                });

        continueButton.setDisable(true);

        ObservableList<String> clients =
                FXCollections.observableArrayList(
                        clientManager.getAllActiveClientNames()
                );

        clientComboBox.setItems(clients);

        clientComboBox.setEditable(true);

        clientComboBox.getEditor()
                .textProperty()
                .addListener((obs, oldValue, newValue) -> {

                    ObservableList<String> filteredList =
                            FXCollections.observableArrayList();

                    for (String client : clients) {

                        if (client.toLowerCase()
                                .contains(newValue.toLowerCase())) {

                            filteredList.add(client);
                        }
                    }

                    clientComboBox.setItems(filteredList);

                    if (!filteredList.isEmpty()) {
                        clientComboBox.show();
                    }
                });
    }

    public void setUserviewController(
            UserviewController controller) {

        this.userviewController = controller;
    }

    private void loadSEAProfiles() {

        currentProfiles.clear();

        currentProfiles.add(
                new Profile(
                        0,
                        0,
                        "Auto Rotate",
                        "Automatically straightens scanned pages"
                ));

        currentProfiles.add(
                new Profile(
                        0,
                        0,
                        "High Brightness",
                        "Brightens dark scanned documents"
                ));

        currentProfiles.add(
                new Profile(
                        0,
                        0,
                        "Archive Quality",
                        "High quality settings for long-term storage"
                ));

        currentProfiles.add(
                new Profile(
                        0,
                        0,
                        "Fast Scan",
                        "Optimized for speed and reduced file size"
                ));

        filterProfiles("");
    }

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

        card.setOnMouseClicked(event -> {

            if (selectedCard != null) {

                selectedCard.getStyleClass()
                        .remove("profile-card-selected");
            }

            selectedCard = card;

            if (!card.getStyleClass()
                    .contains("profile-card-selected")) {

                card.getStyleClass()
                        .add("profile-card-selected");
            }

            selectedProfile = title;

            continueButton.setDisable(false);
        });

        profileContainer.getChildren().add(card);
    }

    @FXML
    private void onContinueClicked() {

        if (selectedProfile == null) {
            return;
        }

        userviewController.setSelectedClient(
                clientComboBox.getEditor().getText()
        );

        userviewController.setScanSetup(
                txtBox.getText(),
                selectedProfile
        );

        Stage stage = (Stage) continueButton
                .getScene()
                .getWindow();

        stage.close();
    }

    @FXML
    private void onCancelClicked() {

        Stage stage = (Stage) cancelButton
                .getScene()
                .getWindow();

        stage.close();
    }

    private void filterProfiles(String searchText) {

        profileContainer.getChildren().clear();

        for (Profile profile : currentProfiles) {

            if (profile.getName()
                    .toLowerCase()
                    .contains(searchText.toLowerCase())) {

                addProfile(
                        profile.getName(),
                        profile.getDescription()
                );
            }
        }
    }
}