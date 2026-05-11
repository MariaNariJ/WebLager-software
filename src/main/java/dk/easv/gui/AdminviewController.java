package dk.easv.gui;

import dk.easv.be.User;
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

    @FXML
    private Button logoutButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button clientsButton;

    @FXML
    private Button logsButton;

    @FXML
    private Label adminTitleLabel;

    @FXML
    private Label adminSubtitleLabel;

    @FXML
    private VBox adminContentArea;

    private final UserDAO userDAO = new UserDAO();

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

        topBar.getChildren().addAll(
                searchField,
                roleFilter,
                spacer,
                btnCreate
        );

        TableView<User> userTable = new TableView<>();
        userTable.setPrefHeight(650);

        TableColumn<User, String> nameCol =
                new TableColumn<>("Name");

        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getName()
                )
        );

        nameCol.setPrefWidth(420);

        TableColumn<User, String> loginCol =
                new TableColumn<>("Login");

        loginCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLogin()
                )
        );

        loginCol.setPrefWidth(320);

        TableColumn<User, String> roleCol =
                new TableColumn<>("Role");

        roleCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getRole()
                )
        );

        roleCol.setPrefWidth(160);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        userTable.getColumns().addAll(
                nameCol,
                roleCol,
                loginCol

        );

        FilteredList<User> filteredUsers =
                new FilteredList<>(
                        FXCollections.observableArrayList(
                                userDAO.getAllUsers()
                        ),
                        p -> true
                );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            String search = newVal.toLowerCase();

            filteredUsers.setPredicate(user -> {

                boolean matchesSearch =
                        user.getName().toLowerCase().contains(search)
                                || user.getLogin().toLowerCase().contains(search);

                String selectedRole = roleFilter.getValue();

                boolean matchesRole =
                        selectedRole.equals("All")
                                || user.getRole().equalsIgnoreCase(selectedRole);

                return matchesSearch && matchesRole;
            });
        });

        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> {

            String search = searchField.getText().toLowerCase();

            filteredUsers.setPredicate(user -> {

                boolean matchesSearch =
                        user.getName().toLowerCase().contains(search)
                                || user.getLogin().toLowerCase().contains(search);

                boolean matchesRole =
                        newVal.equals("All")
                                || user.getRole().equalsIgnoreCase(newVal);

                return matchesSearch && matchesRole;
            });
        });

        userTable.setItems(filteredUsers);

        btnCreate.setOnAction(e -> {

            TextInputDialog dialog = new TextInputDialog();

            dialog.setTitle("Create User");
            dialog.setHeaderText("Temporary create user placeholder");
            dialog.setContentText("This will later open a proper create-user window.");

            dialog.showAndWait();
        });

        wrapper.getChildren().addAll(
                topBar,
                userTable
        );

        VBox.setVgrow(userTable, Priority.ALWAYS);

        adminContentArea.getChildren().add(wrapper);
    }

    private void showClients() {

        adminTitleLabel.setText("Clients");
        adminSubtitleLabel.setText("Manage clients and profiles");

        setInactiveButton(usersButton);
        setActiveButton(clientsButton);
        setInactiveButton(logsButton);

        adminContentArea.getChildren().clear();

        VBox wrapper = new VBox(16);

        Label title = new Label("Clients");
        title.getStyleClass().add("section-title");

        Label text = new Label(
                "Client and profile management will be displayed here."
        );

        text.getStyleClass().add("muted-text");

        wrapper.getChildren().addAll(
                title,
                text
        );

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

        Label title = new Label("Logs");
        title.getStyleClass().add("section-title");

        Label text = new Label(
                "System logs will be displayed here."
        );

        text.getStyleClass().add("muted-text");

        wrapper.getChildren().addAll(
                title,
                text
        );

        adminContentArea.getChildren().add(wrapper);
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
}