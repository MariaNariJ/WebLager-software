package dk.easv.gui;

import dk.easv.be.Client;
import dk.easv.dal.dao.ClientDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminClientsController {

    @FXML
    private VBox clientsRoot;

    private final ClientDAO clientDAO = new ClientDAO();

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

        Button btnCreateClient = new Button("+ Create Client");
        btnCreateClient.getStyleClass().add("primary-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(searchField, spacer, btnCreateClient);

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

        clientsTable.getColumns().addAll(
                clientNameCol,
                profilesCol,
                lastUpdatedCol
        );

        FilteredList<Client> filteredClients = new FilteredList<>(
                FXCollections.observableArrayList(clientDAO.getAllClients()),
                p -> true
        );

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String search = newValue == null ? "" : newValue.toLowerCase();

            filteredClients.setPredicate(client ->
                    safe(client.getName()).toLowerCase().contains(search)
            );
        });

        clientsTable.setItems(filteredClients);

        btnCreateClient.setOnAction(e -> showCreateClientDialog(clientsTable));

        wrapper.getChildren().addAll(topBar, clientsTable);
        VBox.setVgrow(clientsTable, Priority.ALWAYS);

        clientsRoot.getChildren().add(wrapper);
    }

    private void showCreateClientDialog(TableView<Client> clientsTable) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Client");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/app.css").toExternalForm()
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

        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {

                String clientName = txtClientName.getText().trim();

                if (clientName.isEmpty()) {
                    return;
                }

                clientDAO.createClient(clientName);

                clientsTable.setItems(
                        FXCollections.observableArrayList(
                                clientDAO.getAllClients()
                        )
                );
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
}