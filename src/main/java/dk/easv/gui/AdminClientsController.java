package dk.easv.gui;

import dk.easv.be.Client;
import dk.easv.be.Profile;
import dk.easv.bll.ClientManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.StageStyle;

public class AdminClientsController {

    @FXML
    private VBox clientsRoot;

    private final ClientManager clientManager = new ClientManager();

    @FXML
    private void initialize() {
        showClients();
    }

    private void showClients() {

        clientsRoot.getChildren().clear();

        VBox wrapper = new VBox(16);

        HBox topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search clients...");
        searchField.getStyleClass().add("dark-field");
        searchField.setPrefWidth(300);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Active", "Inactive", "All");
        statusFilter.setValue("Active");
        statusFilter.getStyleClass().add("dark-combo");
        statusFilter.setPrefWidth(120);

        Button btnCreateClient = new Button("+ Create Client");
        btnCreateClient.getStyleClass().add("primary-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(searchField, statusFilter, spacer, btnCreateClient);

        TableView<Client> clientsTable = new TableView<>();
        clientsTable.setPrefHeight(650);
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Client, String> clientNameCol = new TableColumn<>("Client Name");
        clientNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getName()))
        );
        clientNameCol.setPrefWidth(600);

        TableColumn<Client, String> profilesCol = new TableColumn<>("Profiles");
        profilesCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getProfilesCount()))
        );
        profilesCol.setPrefWidth(180);

        TableColumn<Client, String> lastUpdatedCol = new TableColumn<>("Last Updated");
        lastUpdatedCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLastUpdated() == null
                                ? "-"
                                : data.getValue().getLastUpdated().toString()
                )
        );
        lastUpdatedCol.setPrefWidth(220);

        TableColumn<Client, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getStatus()))
        );
        statusCol.setPrefWidth(160);

        clientsTable.getColumns().addAll(
                clientNameCol,
                profilesCol,
                lastUpdatedCol,
                statusCol
        );

        FilteredList<Client> filteredClients = new FilteredList<>(
                FXCollections.observableArrayList(clientManager.getAllClients()),
                p -> true
        );

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyClientFilter(filteredClients, searchField, statusFilter);
        });

        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            applyClientFilter(filteredClients, searchField, statusFilter);
        });

        applyClientFilter(filteredClients, searchField, statusFilter);

        clientsTable.setItems(filteredClients);

        clientsTable.setRowFactory(tableView -> {
            TableRow<Client> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();

            MenuItem setActive = new MenuItem("Set as Active");
            MenuItem setInactive = new MenuItem("Set as Inactive");

            setActive.setOnAction(event -> {
                Client selectedClient = row.getItem();

                if (selectedClient == null) {
                    return;
                }

                clientManager.updateClientStatus(selectedClient.getClientId(), "Active");
                selectedClient.setStatus("Active");
                clientsTable.refresh();
            });

            setInactive.setOnAction(event -> {
                Client selectedClient = row.getItem();

                if (selectedClient == null) {
                    return;
                }

                clientManager.updateClientStatus(selectedClient.getClientId(), "Inactive");
                selectedClient.setStatus("Inactive");
                clientsTable.refresh();
            });

            contextMenu.getItems().addAll(setActive, setInactive);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showClientDetails(row.getItem());
                }
            });
            return row;
        });

        btnCreateClient.setOnAction(e -> showCreateClientDialog());

        wrapper.getChildren().addAll(topBar, clientsTable);
        VBox.setVgrow(clientsTable, Priority.ALWAYS);

        clientsRoot.getChildren().add(wrapper);
    }

    private void showCreateClientDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Client");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setPrefWidth(420);

        TextField txtClientName = new TextField();
        txtClientName.setPromptText("Client name");
        txtClientName.getStyleClass().add("dark-field");

        VBox content = new VBox(12);
        content.getChildren().addAll(
                createTitle("Create new client"),
                createText("Enter the name of the client below."),
                txtClientName
        );

        dialogPane.setContent(content);

        ButtonType createButtonType =
                new ButtonType("Create Client", ButtonBar.ButtonData.OK_DONE);

        dialogPane.getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        Button createButton = (Button) dialogPane.lookupButton(createButtonType);
        createButton.getStyleClass().add("dialog-primary-button");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("dialog-secondary-button");

        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {

                String clientName = txtClientName.getText().trim();

                if (clientName.isEmpty()) {
                    return;
                }

                clientManager.createClient(clientName);
                showClients();
            }
        });
    }

    private Label createTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private Label createText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyClientFilter(FilteredList<Client> filteredClients,
                                   TextField searchField,
                                   ComboBox<String> statusFilter) {

        String search = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase();

        String selectedStatus = statusFilter.getValue();

        filteredClients.setPredicate(client -> {

            boolean matchesSearch =
                    safe(client.getName()).toLowerCase().contains(search);

            boolean matchesStatus =
                    selectedStatus.equals("All")
                            || safe(client.getStatus()).equalsIgnoreCase(selectedStatus);

            return matchesSearch && matchesStatus;
        });
    }
    private void showClientDetails(Client client) {

        clientsRoot.getChildren().clear();

        VBox wrapper = new VBox(18);

        HBox topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("small-button");
        backButton.setOnAction(e -> showClients());

        VBox titleBox = new VBox(4);

        Label title = createTitle(client.getName());
        Label subtitle = createText("Manage profiles for this client");

        titleBox.getChildren().addAll(title, subtitle);

        Button createProfileButton = new Button("+ Create Profile");
        createProfileButton.getStyleClass().add("primary-action");

        Button deleteClientButton = new Button("Delete Client");
        deleteClientButton.getStyleClass().add("destructive-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backButton, titleBox, spacer, deleteClientButton, createProfileButton);

        deleteClientButton.setOnAction(e -> {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initStyle(StageStyle.UNDECORATED);
            confirm.setGraphic(null);
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will permanently delete " + client.getName() + " and all its profiles.");

            DialogPane confirmPane = confirm.getDialogPane();

            confirmPane.getStylesheets().add(
                    getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
            );


            confirmPane.getStylesheets().add(
                    getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
            );
            confirmPane.getStyleClass().add("admin-dialog");

            Button okButton = (Button) confirmPane.lookupButton(ButtonType.OK);
            okButton.setText("Delete");
            okButton.getStyleClass().add("destructive-action");

            Button cancelButton = (Button) confirmPane.lookupButton(ButtonType.CANCEL);
            cancelButton.setText("Cancel");
            cancelButton.getStyleClass().add("dialog-secondary-button");

            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    clientManager.deleteClient(client.getClientId());
                    showClients();
                }
            });
        });

        FlowPane profilesPane = new FlowPane();
        profilesPane.setHgap(16);
        profilesPane.setVgap(16);

        for (Profile profile : clientManager.getProfilesByClientId(client.getClientId())) {

            VBox card = new VBox(8);
            card.getStyleClass().add("admin-profile-card");
            card.setPrefWidth(230);
            card.setPrefHeight(120);

            Label profileName = new Label(profile.getName());
            profileName.getStyleClass().add("admin-profile-title");

            Label description = new Label(safe(profile.getDescription()));
            description.getStyleClass().add("admin-profile-description");
            description.setWrapText(true);

            card.getChildren().addAll(profileName, description);

            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    showProfileDetailsDialog(client, profile);
                }
            });

            profilesPane.getChildren().add(card);
        }

        createProfileButton.setOnAction(e -> showCreateProfileDialog(client));

        wrapper.getChildren().addAll(topBar, profilesPane);

        clientsRoot.getChildren().add(wrapper);
    }
    private void showCreateProfileDialog(Client client) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Profile");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setPrefWidth(420);

        TextField txtProfileName = new TextField();
        txtProfileName.setPromptText("Profile name");
        txtProfileName.getStyleClass().add("dark-field");

        TextArea txtDescription = new TextArea();
        txtDescription.setPromptText("Description");
        txtDescription.getStyleClass().add("dark-area");
        txtDescription.setPrefRowCount(4);

        VBox content = new VBox(12);
        content.getChildren().addAll(
                createTitle("Create new profile"),
                createText("Create a scan profile for " + client.getName() + "."),
                txtProfileName,
                txtDescription
        );

        dialogPane.setContent(content);

        ButtonType createButtonType =
                new ButtonType("Create Profile", ButtonBar.ButtonData.OK_DONE);

        dialogPane.getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        Button createButton = (Button) dialogPane.lookupButton(createButtonType);
        createButton.getStyleClass().add("dialog-primary-button");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("dialog-secondary-button");

        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {

                String profileName = txtProfileName.getText().trim();
                String description = txtDescription.getText().trim();

                if (profileName.isEmpty()) {
                    return;
                }

                clientManager.createProfile(client.getClientId(), profileName, description);
                showClientDetails(client);
            }
        });
    }
    private void showProfileDetailsDialog(Client client, Profile profile) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Profile Details");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setPrefWidth(420);

        TextField txtName = new TextField(profile.getName());
        txtName.getStyleClass().add("dark-field");

        TextArea txtDescription = new TextArea(safe(profile.getDescription()));
        txtDescription.getStyleClass().add("dark-area");
        txtDescription.setPrefRowCount(4);

        VBox content = new VBox(12);
        content.getChildren().addAll(
                createTitle("Profile details"),
                createText("Edit or delete this scan profile."),
                txtName,
                txtDescription
        );

        dialogPane.setContent(content);

        ButtonType deleteButtonType =
                new ButtonType("Delete Profile", ButtonBar.ButtonData.LEFT);

        ButtonType updateButtonType =
                new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);

        dialogPane.getButtonTypes().addAll(
                deleteButtonType,
                ButtonType.CANCEL,
                updateButtonType
        );

        Button deleteButton = (Button) dialogPane.lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("destructive-action");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("dialog-secondary-button");

        Button updateButton = (Button) dialogPane.lookupButton(updateButtonType);
        updateButton.getStyleClass().add("dialog-primary-button");

        dialog.showAndWait().ifPresent(result -> {

            if (result == updateButtonType) {

                String name = txtName.getText().trim();
                String description = txtDescription.getText().trim();

                if (name.isEmpty()) {
                    return;
                }

                clientManager.updateProfile(
                        profile.getProfileId(),
                        client.getClientId(),
                        name,
                        description
                );

                showClientDetails(client);
            }

            if (result == deleteButtonType) {
                clientManager.deleteProfile(
                        profile.getProfileId(),
                        client.getClientId()
                );

                showClientDetails(client);
            }
        });
    }
}