package dk.easv.gui;

import dk.easv.be.Client;
import dk.easv.be.Profile;
import dk.easv.bll.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class SelectProfileController {

    @FXML
    private VBox profileContainer;

    @FXML
    private ScrollPane profileScrollPane;

    @FXML
    private ListView<String> clientListView;

    @FXML
    private TextField txtBox;

    @FXML
    private Button continueButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField txtClientSearch;

    @FXML
    private TextField txtProfileSearch;

    private HBox selectedCard;

    private boolean hHeld = false;

    private final List<HBox> profileCards =
            new ArrayList<>();

    private final List<Tooltip> profileTooltips =
            new ArrayList<>();

    private int focusedProfileIndex = 0;

    private String selectedProfile;

    private final List<Profile> currentProfiles =
            new ArrayList<>();

    private UserviewController userviewController;

    private final ClientManager clientManager =
            new ClientManager();

    private ObservableList<String> allClients;

    @FXML
    public void initialize() {

        txtBox.setText("BOX_001");

        txtProfileSearch.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterProfiles(newValue)
        );

        txtProfileSearch.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case DOWN:

                    if (focusedProfileIndex
                            < profileCards.size() - 1) {

                        hideFocusedTooltip();

                        focusedProfileIndex++;

                        updateFocusedProfile();

                        if (hHeld) {
                            showFocusedTooltip();
                        }
                    }

                    event.consume();

                    break;

                case UP:

                    if (focusedProfileIndex > 0) {

                        hideFocusedTooltip();

                        focusedProfileIndex--;

                        updateFocusedProfile();

                        if (hHeld) {
                            showFocusedTooltip();
                        }
                    }

                    event.consume();

                    break;
            }
        });

        txtProfileSearch.sceneProperty().addListener(
                (obs, oldScene, newScene) -> {

                    if (newScene == null) {
                        return;
                    }

                    newScene.setOnKeyPressed(event -> {

                        switch (event.getCode()) {

                            case F1:

                                if (!hHeld) {

                                    hHeld = true;

                                    showFocusedTooltip();
                                }

                                event.consume();

                                break;

                            case ENTER:

                                onContinueClicked();

                                event.consume();

                                break;

                            case ESCAPE:

                                onCancelClicked();

                                event.consume();

                                break;
                        }
                    });

                    newScene.setOnKeyReleased(event -> {

                        switch (event.getCode()) {

                            case F1:

                                if (hHeld) {

                                    hHeld = false;

                                    hideFocusedTooltip();

                                    event.consume();
                                }

                                break;
                        }
                    });
                });

        continueButton.setDisable(true);

        txtBox.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        validateForm()
        );

        allClients =
                FXCollections.observableArrayList(
                        clientManager.getAllActiveClientNames()
                );

        clientListView.setOpacity(0);
        clientListView.setPrefHeight(0);

        txtClientSearch.textProperty().addListener(
                (obs, oldValue, newValue) -> {

                    if (newValue.isBlank()) {

                        clientListView.setOpacity(0);
                        clientListView.setPrefHeight(0);

                        return;
                    }

                    ObservableList<String> filtered =
                            FXCollections.observableArrayList();

                    ObservableList<String> startsWith =
                            FXCollections.observableArrayList();

                    ObservableList<String> contains =
                            FXCollections.observableArrayList();

                    for (String client : allClients) {

                        String lower =
                                client.toLowerCase();

                        String search =
                                newValue.toLowerCase();

                        if (lower.startsWith(search)) {

                            startsWith.add(client);
                        }

                        else if (lower.contains(search)) {

                            contains.add(client);
                        }
                    }

                    filtered.addAll(startsWith);
                    filtered.addAll(contains);

                    clientListView.setItems(filtered);

                    clientListView.setOpacity(1);

                    clientListView.setPrefHeight(
                            Math.min(filtered.size() * 40, 80)
                    );
                });

        txtClientSearch.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case ENTER:

                    if (!clientListView.getItems().isEmpty()) {

                        String selectedClient =
                                clientListView
                                        .getItems()
                                        .get(0);

                        txtClientSearch.setText(selectedClient);

                        clientListView.setOpacity(0);

                        clientListView.setPrefHeight(0);

                        loadProfilesForClient(selectedClient);

                        validateForm();

                        txtBox.requestFocus();
                    }

                    event.consume();

                    break;
            }
        });

        txtBox.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case ENTER:

                    txtProfileSearch.requestFocus();

                    event.consume();

                    break;
            }
        });

        clientListView.setOnMouseClicked(event -> {

            String selected =
                    clientListView.getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {
                return;
            }

            txtClientSearch.setText(selected);

            clientListView.setOpacity(0);
            clientListView.setPrefHeight(0);

            loadProfilesForClient(selected);

            validateForm();
        });

        showDefaultProfiles();
    }

    private void showDefaultProfiles() {

        currentProfiles.clear();

        currentProfiles.add(
                new Profile(
                        0,
                        0,
                        "Standard Scan",
                        "Balanced settings for everyday scanning"
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

    public void setUserviewController(
            UserviewController controller) {

        this.userviewController = controller;
    }

    private void loadProfilesForClient(String clientName) {

        currentProfiles.clear();

        profileContainer.getChildren().clear();

        selectedProfile = null;
        selectedCard = null;

        validateForm();

        if (clientName == null || clientName.isBlank()) {
            return;
        }

        Client client =
                clientManager.getClientByName(clientName);

        if (client == null) {
            return;
        }

        currentProfiles.addAll(
                clientManager.getProfilesByClientId(
                        client.getClientId()
                )
        );

        filterProfiles("");
    }

    private void addProfile(
            String title,
            String description) {

        HBox card = new HBox();

        card.getStyleClass().add("profile-card");

        card.setAlignment(Pos.CENTER_LEFT);

        card.setFocusTraversable(true);

        card.setPrefHeight(36);
        card.setMinHeight(36);
        card.setMaxHeight(36);

        Label titleLabel = new Label(title);

        Region spacer = new Region();

        titleLabel.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        titleLabel.getStyleClass().add("profile-title");

        Label infoLabel = new Label("?");

        infoLabel.getStyleClass()
                .add("profile-info-icon");

        Tooltip tooltip =
                new Tooltip(description);

        tooltip.setShowDelay(Duration.millis(150));

        Tooltip.install(infoLabel, tooltip);

        profileCards.add(card);
        profileTooltips.add(tooltip);

        card.getChildren().addAll(
                titleLabel,
                spacer,
                infoLabel
        );

        card.setOnMouseClicked(event -> {

            if (selectedCard == card) {

                card.getStyleClass()
                        .remove("profile-card-selected");

                selectedCard = null;
                selectedProfile = null;

                validateForm();

                return;
            }

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

            validateForm();
        });

        profileContainer.getChildren().add(card);
    }

    @FXML
    private void onContinueClicked() {

        if (selectedProfile == null) {
            return;
        }

        userviewController.setScanSetup(
                txtClientSearch.getText(),
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

        profileCards.clear();
        profileTooltips.clear();

        focusedProfileIndex = 0;

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

        updateFocusedProfile();
    }

    private void showFocusedTooltip() {

        if (profileCards.isEmpty()
                || profileTooltips.isEmpty()) {
            return;
        }

        Tooltip tooltip =
                profileTooltips.get(focusedProfileIndex);

        HBox focusedCard =
                profileCards.get(focusedProfileIndex);

        Label infoLabel =
                (Label) focusedCard.getChildren().get(2);

        tooltip.show(
                infoLabel,
                infoLabel.localToScreen(25, 10).getX(),
                infoLabel.localToScreen(25, 10).getY()
        );
    }

    private void hideFocusedTooltip() {

        if (profileTooltips.isEmpty()) {
            return;
        }

        profileTooltips.get(focusedProfileIndex).hide();
    }

    private void updateFocusedProfile() {

        if (profileCards.isEmpty()) {
            return;
        }

        for (HBox card : profileCards) {

            card.getStyleClass()
                    .remove("profile-card-focused");

            card.getStyleClass()
                    .remove("profile-card-selected");
        }

        HBox focusedCard =
                profileCards.get(focusedProfileIndex);

        focusedCard.getStyleClass()
                .add("profile-card-focused");

        focusedCard.getStyleClass()
                .add("profile-card-selected");

        selectedCard = focusedCard;

        Label titleLabel =
                (Label) focusedCard.getChildren().get(0);

        selectedProfile = titleLabel.getText();

        double contentHeight =
                profileContainer.getBoundsInLocal().getHeight();

        double y =
                focusedCard.getBoundsInParent().getMinY();

        double height =
                focusedCard.getBoundsInParent().getHeight();

        double viewportHeight =
                profileScrollPane.getViewportBounds().getHeight();

        double vValue =
                (y + height / 2.0 - viewportHeight / 2.0)
                        / (contentHeight - viewportHeight);

        profileScrollPane.setVvalue(
                Math.max(0, Math.min(vValue, 1))
        );
    }

    private void validateForm() {

        boolean hasClient =
                !txtClientSearch.getText()
                        .trim()
                        .isEmpty();

        boolean hasBox =
                !txtBox.getText()
                        .trim()
                        .isEmpty();

        boolean hasProfile =
                selectedProfile != null;

        continueButton.setDisable(
                !(hasClient && hasBox && hasProfile)
        );
    }
}