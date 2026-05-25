package dk.easv.gui;

import dk.easv.be.User;
import dk.easv.bll.UserManager;
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
import javafx.stage.StageStyle;

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

    private final UserManager userManager = new UserManager();

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

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Active", "Inactive", "All");
        statusFilter.setValue("Active");
        statusFilter.getStyleClass().add("dark-combo");
        statusFilter.setPrefWidth(120);

        Button btnCreate = new Button("+ Create User");
        btnCreate.getStyleClass().add("primary-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(searchField, roleFilter, statusFilter, spacer, btnCreate);

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

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getStatus()))
        );
        statusCol.setPrefWidth(160);

        userTable.getColumns().addAll(nameCol, roleCol, loginCol, statusCol);

        FilteredList<User> filteredUsers = new FilteredList<>(
                FXCollections.observableArrayList(userManager.getAllUsers()),
                p -> true
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter)
        );

        roleFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter)
        );

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter)
        );

        applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter);

        userTable.setItems(filteredUsers);

        userTable.setRowFactory(tableView -> {
            TableRow<User> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();

            MenuItem setActive = new MenuItem("Set as Active");
            MenuItem setInactive = new MenuItem("Set as Inactive");

            setActive.setOnAction(event -> {
                User selectedUser = row.getItem();

                if (selectedUser == null) {
                    return;
                }

                userManager.updateUserStatus(selectedUser.getId(), "Active");
                selectedUser.setStatus("Active");
                userTable.refresh();
                applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter);
            });

            setInactive.setOnAction(event -> {
                User selectedUser = row.getItem();

                if (selectedUser == null) {
                    return;
                }

                userManager.updateUserStatus(selectedUser.getId(), "Inactive");
                selectedUser.setStatus("Inactive");
                userTable.refresh();
                applyUserFilter(filteredUsers, searchField, roleFilter, statusFilter);
            });

            contextMenu.getItems().addAll(setActive, setInactive);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showUserDetailsDialog(row.getItem());
                }
            });

            return row;
        });

        btnCreate.setOnAction(e -> showCreateUserDialog());

        wrapper.getChildren().addAll(topBar, userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        adminContentArea.getChildren().add(wrapper);
    }

    private void showCreateUserDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create User");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );


        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
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

                userManager.createUser(name, login, password, role);
                showUsers();
            }
        });
    }

    private void showClients() {

        adminTitleLabel.setText("Clients");
        adminSubtitleLabel.setText("Manage clients and profiles");

        setInactiveButton(usersButton);
        setActiveButton(clientsButton);
        setInactiveButton(logsButton);

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/dk/easv/gui/admin-clients.fxml")
            );

            adminContentArea.getChildren().setAll(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLogs() {

        adminTitleLabel.setText("Logs");
        adminSubtitleLabel.setText("View and monitor system logs and activities");

        setInactiveButton(usersButton);
        setInactiveButton(clientsButton);
        setActiveButton(logsButton);

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/dk/easv/gui/admin-logs.fxml")
            );

            adminContentArea.getChildren().setAll(root);

            if (root instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }

            VBox.setVgrow(root, Priority.ALWAYS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyUserFilter(FilteredList<User> filteredUsers,
                                 TextField searchField,
                                 ComboBox<String> roleFilter,
                                 ComboBox<String> statusFilter) {

        String search = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase();

        String selectedRole = roleFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        filteredUsers.setPredicate(user -> {

            String name = safe(user.getName()).toLowerCase();
            String login = safe(user.getLogin()).toLowerCase();
            String role = safe(user.getRole());
            String status = safe(user.getStatus());

            boolean matchesSearch =
                    name.contains(search) || login.contains(search);

            boolean matchesRole =
                    selectedRole.equals("All") || role.equalsIgnoreCase(selectedRole);

            boolean matchesStatus =
                    selectedStatus.equals("All") || status.equalsIgnoreCase(selectedStatus);

            return matchesSearch && matchesRole && matchesStatus;
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
        dialog.initStyle(StageStyle.UNDECORATED);

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
        );


        dialogPane.getStylesheets().add(
                getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("admin-dialog");

        dialogPane.setPrefWidth(360);
        dialogPane.setPrefHeight(330);

        TextField txtName = new TextField(user.getName());
        txtName.getStyleClass().add("dark-field");

        TextField txtLogin = new TextField(user.getLogin());
        txtLogin.getStyleClass().add("dark-field");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("User", "Admin");
        roleBox.setValue(user.getRole());
        roleBox.getStyleClass().add("dark-combo");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Inactive");
        statusBox.setValue(user.getStatus());
        statusBox.getStyleClass().add("dark-combo");
        statusBox.setMaxWidth(Double.MAX_VALUE);

        PasswordField txtNewPassword = new PasswordField();
        txtNewPassword.setPromptText("New password - leave empty to keep current");
        txtNewPassword.getStyleClass().add("dark-field");

        ButtonType deleteButtonType = new ButtonType("Delete User", ButtonBar.ButtonData.LEFT);
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);

        dialogPane.getButtonTypes().addAll(deleteButtonType, ButtonType.CLOSE, updateButtonType);

        VBox content = new VBox(12);
        content.setPrefWidth(320);
        content.setStyle("-fx-padding: 18;");

        content.getChildren().addAll(
                createTitle("User details"),
                createText("Edit user information or reset the password."),
                txtName,
                txtLogin,
                roleBox,
                statusBox,
                txtNewPassword
        );

        dialogPane.setContent(content);

        Button deleteButton = (Button) dialogPane.lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("destructive-action");

        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.getStyleClass().add("dialog-secondary-button");

        Button updateButton = (Button) dialogPane.lookupButton(updateButtonType);
        updateButton.getStyleClass().add("dialog-primary-button");

        dialog.showAndWait().ifPresent(result -> {

            if (result == updateButtonType) {

                user.setName(txtName.getText().trim());
                user.setLogin(txtLogin.getText().trim());
                user.setRole(roleBox.getValue());
                user.setStatus(statusBox.getValue());

                userManager.updateUser(user);

                String newPassword = txtNewPassword.getText().trim();

                if (!newPassword.isEmpty()) {
                    userManager.updateUserPassword(user.getId(), newPassword);
                }

                showUsers();
            }

            if (result == deleteButtonType) {

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.initStyle(StageStyle.UNDECORATED);
                confirm.setGraphic(null);
                confirm.setHeaderText("Are you sure?");
                confirm.setContentText("This will permanently delete " + user.getName() + ".");

                DialogPane confirmPane = confirm.getDialogPane();

                confirmPane.getStylesheets().add(
                        getClass().getResource("/dk/easv/gui/css/app.css").toExternalForm()
                );


                confirmPane.getStylesheets().add(
                        getClass().getResource("/dk/easv/gui/css/admin.css").toExternalForm()
                );
                confirmPane.getStyleClass().add("admin-dialog");
                confirmPane.setPrefWidth(380);

                Button okButton = (Button) confirmPane.lookupButton(ButtonType.OK);
                okButton.setText("Delete");
                okButton.getStyleClass().add("destructive-action");

                Button cancelButton = (Button) confirmPane.lookupButton(ButtonType.CANCEL);
                cancelButton.setText("Cancel");

                confirm.showAndWait().ifPresent(confirmResult -> {
                    if (confirmResult == ButtonType.OK) {
                        userManager.deleteUser(user.getId());
                        showUsers();
                    }
                });
            }
        });
    }
}