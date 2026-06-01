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
import dk.easv.bll.LogManager;
import dk.easv.be.User;

public class AdminClientsController {

    @FXML private VBox clientsRoot;
    @FXML private VBox clientsOverview;
    @FXML private VBox clientDetailsContainer;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnCreateClient;
    @FXML private TableView<Client> clientsTable;

    private final ClientManager clientManager = new ClientManager();
    private final LogManager logManager = new LogManager();
    private User loggedInUser;

    @FXML
    private void initialize() {
        setupClientsTable();
        setupFilters();
        showClients();
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("Active", "Inactive", "All");
        statusFilter.setValue("Active");

        searchField.textProperty().addListener((obs, oldValue, newValue) -> loadClients());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> loadClients());

        btnCreateClient.setOnAction(e -> showCreateClientDialog());
    }

    private void setupClientsTable() {
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

                createAdminLog(
                        "Client Activated",
                        "Activated client: " + selectedClient.getName(),
                        "Completed"
                );

                selectedClient.setStatus("Active");
                clientsTable.refresh();
                loadClients();
            });

            setInactive.setOnAction(event -> {
                Client selectedClient = row.getItem();

                if (selectedClient == null) {
                    return;
                }

                clientManager.updateClientStatus(selectedClient.getClientId(), "Inactive");

                createAdminLog(
                        "Client Deactivated",
                        "Deactivated client: " + selectedClient.getName(),
                        "Completed"
                );

                selectedClient.setStatus("Inactive");
                clientsTable.refresh();
                loadClients();
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
    }

    private void showClients() {
        clientsOverview.setVisible(true);
        clientsOverview.setManaged(true);

        clientDetailsContainer.setVisible(false);
        clientDetailsContainer.setManaged(false);
        clientDetailsContainer.getChildren().clear();

        loadClients();
    }

    private void loadClients() {
        FilteredList<Client> filteredClients = new FilteredList<>(
                FXCollections.observableArrayList(clientManager.getAllClients()),
                p -> true
        );

        applyClientFilter(filteredClients, searchField, statusFilter);
        clientsTable.setItems(filteredClients);
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

                createAdminLog(
                        "Client Created",
                        "Created client: " + clientName,
                        "Completed"
                );

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

        clientsOverview.setVisible(false);
        clientsOverview.setManaged(false);

        clientDetailsContainer.setVisible(true);
        clientDetailsContainer.setManaged(true);
        clientDetailsContainer.getChildren().clear();

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
        createProfileButton.getStyleClass().addAll("primary-action", "client-detail-action-button");

        Button deleteClientButton = new Button("Delete Client");
        deleteClientButton.getStyleClass().addAll("destructive-action", "client-detail-action-button");

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

                    createAdminLog(
                            "Client Deleted",
                            "Deleted client: " + client.getName(),
                            "Completed"
                    );

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

        clientDetailsContainer.getChildren().add(wrapper);
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
        txtDescription.setWrapText(true);
        txtDescription.setMinHeight(120);
        txtDescription.setPrefHeight(120);
        txtDescription.setMaxHeight(120);

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

                createAdminLog(
                        "Profile Created",
                        "Created profile: " + profileName + " for client: " + client.getName(),
                        "Completed"
                );

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
        txtDescription.setWrapText(true);
        txtDescription.setMinHeight(120);
        txtDescription.setPrefHeight(120);
        txtDescription.setMaxHeight(120);

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
        deleteButton.getStyleClass().clear();
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

                createAdminLog(
                        "Profile Updated",
                        "Updated profile: " + name + " for client: " + client.getName(),
                        "Completed"
                );

                showClientDetails(client);
            }

            if (result == deleteButtonType) {
                clientManager.deleteProfile(
                        profile.getProfileId(),
                        client.getClientId()
                );

                createAdminLog(
                        "Profile Deleted",
                        "Deleted profile: " + profile.getName() + " from client: " + client.getName(),
                        "Completed"
                );

                showClientDetails(client);
            }
        });
    }
    private void createAdminLog(String event, String details, String status) {
        Integer userId = loggedInUser != null ? loggedInUser.getId() : null;

        logManager.createLog(
                "Info",
                "User",
                event,
                userId,
                details,
                status,
                "00:00:00"
        );
    }
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }
}