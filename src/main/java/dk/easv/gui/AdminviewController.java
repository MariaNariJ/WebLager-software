package dk.easv.gui;

import dk.easv.be.Client;
import dk.easv.be.User;
import dk.easv.dal.dao.ClientDAO;
import dk.easv.dal.dao.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminviewController {

    @FXML private Button logoutButton;
    @FXML private Button usersButton;
    @FXML private Button clientsButton;
    @FXML private Button logsButton;

    @FXML private Label adminTitleLabel;
    @FXML private Label adminSubtitleLabel;

    @FXML private VBox adminContentArea;

    @FXML private Label adminNameLabel;
    @FXML private Label adminRoleLabel;

    private User loggedInUser;

    private final UserDAO userDAO = new UserDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @FXML
    private void initialize() {
        showUsers();
    }

    @FXML
    private void onUsersClicked() {
        showUsers();
    }

    @FXML
    private void onClientsClicked() {
        showClients();
    }

    @FXML
    private void onLogsClicked() {
        showLogs();
    }

    private void showUsers() {

        adminTitleLabel.setText("Users");
        adminSubtitleLabel.setText("Manage employee accounts and roles");

        setActiveButton(usersButton);
        setInactiveButton(clientsButton);
        setInactiveButton(logsButton);

        adminContentArea.getChildren().clear();

        VBox wrapper = new VBox(16);

        HBox topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.getStyleClass().add("dark-field");
        searchField.setPrefWidth(260);

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All", "Admin", "User");
        roleFilter.setValue("All");
        roleFilter.getStyleClass().add("dark-combo");
        roleFilter.setPrefWidth(120);

        Button btnCreate = new Button("+ Create User");
        btnCreate.getStyleClass().add("primary-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(searchField, roleFilter, spacer, btnCreate);

        TableView<User> userTable = new TableView<>();
        userTable.setPrefHeight(650);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getName()))
        );
        nameCol.setPrefWidth(420);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getRole()))
        );
        roleCol.setPrefWidth(160);

        TableColumn<User, String> loginCol = new TableColumn<>("Login");
        loginCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getLogin()))
        );
        loginCol.setPrefWidth(320);

        userTable.getColumns().addAll(nameCol, roleCol, loginCol);

        FilteredList<User> filteredUsers = new FilteredList<>(
                FXCollections.observableArrayList(userDAO.getAllUsers()),
                p -> true
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyUserFilter(filteredUsers, searchField, roleFilter);
        });

        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyUserFilter(filteredUsers, searchField, roleFilter);
        });

        userTable.setItems(filteredUsers);
        userTable.setRowFactory(tableView -> {
            TableRow<User> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User selectedUser = row.getItem();
                    showUserDetailsDialog(selectedUser);
                }
            });

            return row;
        });

        btnCreate.setOnAction(e -> showCreateUserDialog(userTable));

        wrapper.getChildren().addAll(topBar, userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        adminContentArea.getChildren().add(wrapper);
    }

    private void showCreateUserDialog(TableView<User> userTable) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create User");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/app.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("custom-dialog");

        TextField txtName = new TextField();
        txtName.setPromptText("Full name");
        txtName.getStyleClass().add("dark-field");

        TextField txtLogin = new TextField();
        txtLogin.setPromptText("Login");
        txtLogin.getStyleClass().add("dark-field");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.getStyleClass().add("dark-field");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("User", "Admin");
        roleBox.setValue("User");
        roleBox.getStyleClass().add("dark-combo");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(12);
        content.getChildren().addAll(
                createTitle("Create new user"),
                createText("Fill in the employee information below."),
                txtName,
                txtLogin,
                txtPassword,
                roleBox
        );

        dialogPane.setContent(content);

        ButtonType createButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {

                String name = txtName.getText().trim();
                String login = txtLogin.getText().trim();
                String password = txtPassword.getText().trim();
                String role = roleBox.getValue();

                if (name.isEmpty() || login.isEmpty() || password.isEmpty() || role == null) {
                    return;
                }

                userDAO.createUser(name, login, password, role);

                userTable.setItems(
                        FXCollections.observableArrayList(
                                userDAO.getAllUsers()
                        )
                );
            }
        });
    }

    private void showClients() {

        adminTitleLabel.setText("Clients");
        adminSubtitleLabel.setText("Manage clients and profiles");

        setInactiveButton(usersButton);
        setActiveButton(clientsButton);
        setInactiveButton(logsButton);

        adminContentArea.getChildren().clear();

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

        adminContentArea.getChildren().add(wrapper);
    }

    private void showLogs() {

        adminTitleLabel.setText("Logs");
        adminSubtitleLabel.setText("View system activity and user actions");

        setInactiveButton(usersButton);
        setInactiveButton(clientsButton);
        setActiveButton(logsButton);

        adminContentArea.getChildren().clear();

        VBox wrapper = new VBox(16);

        wrapper.getChildren().addAll(
                createTitle("Logs"),
                createText("System logs will be displayed here.")
        );

        adminContentArea.getChildren().add(wrapper);
    }

    private void applyUserFilter(FilteredList<User> filteredUsers,
                                 TextField searchField,
                                 ComboBox<String> roleFilter) {

        String search = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase();

        String selectedRole = roleFilter.getValue();

        filteredUsers.setPredicate(user -> {

            String name = safe(user.getName()).toLowerCase();
            String login = safe(user.getLogin()).toLowerCase();
            String role = safe(user.getRole());

            boolean matchesSearch =
                    name.contains(search) || login.contains(search);

            boolean matchesRole =
                    selectedRole.equals("All") || role.equalsIgnoreCase(selectedRole);

            return matchesSearch && matchesRole;
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

    private void setActiveButton(Button button) {
        button.getStyleClass().remove("nav-button");

        if (!button.getStyleClass().contains("nav-button-active")) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    private void setInactiveButton(Button button) {
        button.getStyleClass().remove("nav-button-active");

        if (!button.getStyleClass().contains("nav-button")) {
            button.getStyleClass().add("nav-button");
        }
    }

    @FXML
    private void onLogOutClicked() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/dk/easv/gui/log-in.fxml")
            );

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;

        adminNameLabel.setText(user.getName());
        adminRoleLabel.setText(user.getRole());
    }
    private void showUserDetailsDialog(User user) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Details");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/app.css").toExternalForm()
        );

        dialogPane.getStyleClass().add("custom-dialog");

        dialogPane.setPrefWidth(500);
        dialogPane.setPrefHeight(350);

        VBox content = new VBox(18);

        content.setPrefWidth(450);
        content.setStyle("-fx-padding: 20;");

        Label title = createTitle("User details");

        Separator separator = new Separator();

        VBox infoBox = new VBox(14);

        Label name = createText("Name: " + safe(user.getName()));
        Label role = createText("Role: " + safe(user.getRole()));
        Label login = createText("Login: " + safe(user.getLogin()));
        infoBox.getChildren().addAll(name, role, login);
        content.getChildren().addAll(
                title,
                separator,
                infoBox
        );
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
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

        ButtonType createButtonType = new ButtonType("Create Client", ButtonBar.ButtonData.OK_DONE);
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

}