package dk.easv.gui;

import dk.easv.bll.PasswordManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML
    private Label loginMessageLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Pane loginPatternPane;

    private final PasswordManager passwordManager = new PasswordManager();
    //    private final PasswordHasher hash = new PasswordHasher();

    private Image backgroundLogo;

    public void initialize() {
        //CreateUser();

        signInButton.setDefaultButton(true);

        backgroundLogo = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/dk/easv/gui/assets/LogoBlue_Logoicon.png")
                )
        );

        Platform.runLater(this::generateResponsiveBackgroundLogos);

        loginPatternPane.widthProperty().addListener((obs, oldValue, newValue) -> generateResponsiveBackgroundLogos());
        loginPatternPane.heightProperty().addListener((obs, oldValue, newValue) -> generateResponsiveBackgroundLogos());
    }

    private void generateResponsiveBackgroundLogos() {
        loginPatternPane.getChildren().clear();

        double width = loginPatternPane.getWidth();
        double height = loginPatternPane.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        double spacingX = 160;
        double spacingY = 130;
        double logoSize = 52;

        int columns = (int) Math.ceil(width / spacingX) + 1;
        int rows = (int) Math.ceil(height / spacingY) + 1;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {

                ImageView logo = new ImageView(backgroundLogo);

                logo.setFitWidth(logoSize);
                logo.setPreserveRatio(true);
                logo.setSmooth(true);
                logo.setMouseTransparent(true);
                logo.getStyleClass().add("watermark-image");

                double xOffset = row % 2 == 0 ? 55 : 95;
                double yOffset = col % 2 == 0 ? 55 : 95;

                logo.setLayoutX((col * spacingX) + xOffset);
                logo.setLayoutY((row * spacingY) + yOffset);

                loginPatternPane.getChildren().add(logo);
            }
        }
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

                    if ("Inactive".equalsIgnoreCase(user.getStatus())) {
                        loginMessageLabel.setText("Your account is inactive. Please contact an admin.");
                        loginMessageLabel.getStyleClass().remove("login-subtitle");
                        loginMessageLabel.getStyleClass().add("login-error-message");

                        signInButton.setDisable(false);
                        signInButton.setOpacity(1);

                        return;
                    }

                    try {

                        Stage stage =
                                (Stage) usernameField.getScene().getWindow();

                        if (user.getRole().equalsIgnoreCase("Admin")) {

                            FXMLLoader loader = new FXMLLoader(
                                    Objects.requireNonNull(
                                            getClass().getResource(
                                                    "/dk/easv/gui/adminview.fxml"
                                            )
                                    )
                            );

                            stage.getScene().setRoot(loader.load());

                            AdminviewController controller =
                                    loader.getController();

                            controller.setLoggedInUser(user);

                            System.out.println("Logged in as Admin");

                        } else {

                            FXMLLoader loader = new FXMLLoader(
                                    Objects.requireNonNull(
                                            getClass().getResource(
                                                    "/dk/easv/gui/userview.fxml"
                                            )
                                    )
                            );

                            stage.getScene().setRoot(loader.load());

                            UserviewController controller =
                                    loader.getController();

                            controller.setLoggedInUser(user);

                            System.out.println("Logged in as User");
                        }

                    } catch (Exception e) {

                        System.err.println(
                                "Failed changing stage " + e.getMessage()
                        );

                        e.printStackTrace();

                        signInButton.setDisable(false);
                        signInButton.setOpacity(1);
                    }

                } else {

                    loginMessageLabel.setText(
                            "Wrong username or password"
                    );

                    loginMessageLabel.getStyleClass()
                            .remove("login-subtitle");

                    loginMessageLabel.getStyleClass()
                            .add("login-error-message");

                    System.out.println(
                            "Wrong username or password"
                    );

                    signInButton.setDisable(false);
                    signInButton.setOpacity(1);
                }
            });

        }).exceptionally(ex -> {

            Platform.runLater(() -> {

                System.err.println(
                        "An error occurred during login: "
                                + ex.getMessage()
                );

                signInButton.setDisable(false);
                signInButton.setOpacity(1);
            });

            return null;
        });
    }

    //    This method is used to create users in the database until there is a feature to create users
    //    public void CreateUser()
    //    {
    //        String login = "Username";
    //        String password = "u";
    //        try
    //        {
    //            String salt = hash.generateSalt();
    //            String hashedPassword = hash.hashPassword(password, salt);
    //            System.out.println(salt);
    //            System.out.println(hashedPassword);
    //        } catch (Exception e) {
    //            System.out.println("Failed generating User" + e.getMessage());
    //        }
    //    }
}