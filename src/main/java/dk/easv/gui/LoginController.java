package dk.easv.gui;

import dk.easv.be.User;
import dk.easv.bll.PasswordManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import java.util.Objects;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMeCheckBox;
    @FXML
    private Button signInButton;
    @FXML
    private final PasswordManager passwordManager  = new PasswordManager();

    public void initialize()
    {
//        CreateUser();
    }

    public void btnSingIn() {
        String login = usernameField.getText().toLowerCase();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill in all fields");
            return;
        }

        signInButton.setDisable(true);
        signInButton.setOpacity(0.6);

        CompletableFuture.supplyAsync(() -> {
            boolean isValid = passwordManager.checkPassword(login, password);

            if (isValid) {
                return passwordManager.getUser();
            } else {
                return null;
            }
        }).thenAccept(user -> {
            Platform.runLater(() -> {
                if (user != null) {
                    try {
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        if (user.getRole().equals("Admin")) {
                            //Change stage to Admin page view
                            System.out.println("Logged in as Admin");
                        } else {
                            stage.getScene().setRoot(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../gui/userview.fxml"))));
                            System.out.println("Logged in as User");
                        }
                    } catch (Exception e) {
                        System.err.println("Failed changing stage " + e.getMessage());
                    }
                } else {
                    System.out.println("Wrong username or password");
                    signInButton.setDisable(false);
                    signInButton.setOpacity(1);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("An error occurred during login: " + ex.getMessage());
                signInButton.setDisable(false);
                signInButton.setOpacity(1);
            });
            return null;
        });
    }

    /*
    This method is used to create users in the database until there is a feature to create users

    public void CreateUser()
    {
        String login = "rocky";
        String password = "r";
        try
        {
            String salt = hash.generateSalt();
            String hashedPassword = hash.hashPassword(password, salt);
            System.out.println(salt);
            System.out.println(hashedPassword);
        } catch (Exception e) {
            System.out.println("Failed generating User" + e.getMessage());
        }
    }
    */
}
