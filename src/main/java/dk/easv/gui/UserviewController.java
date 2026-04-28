package dk.easv.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.TranslateTransition;
import javafx.stage.Stage;
import javafx.util.Duration;

import dk.easv.bll.FileManager;
import dk.easv.be.Page;

import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

public class UserviewController {

    private final FileManager fileManager = new FileManager();

    private List<Page> scannedPages;

    @FXML
    private VBox fileListContainer;

    @FXML
    private VBox sidebar;

    @FXML
    private Pane sidebarTrigger;

    @FXML
    private Button sidebarLockButton;

    @FXML
    private Label barcodeLabel;

    @FXML
    private ImageView previewImage;

    @FXML
    private TextField searchField;

    private boolean sidebarVisible = false;
    private boolean sidebarLocked = false;

    // ================= Initialize =================
    @FXML
    public void initialize() {

        sidebarTrigger.setOnMouseEntered(e -> showSidebar());
        sidebar.setOnMouseExited(e -> hideSidebar());
        sidebarTrigger.toFront();

        // Search listener
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterFiles(newValue);
        });

        Platform.runLater(() -> {
            sidebar.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.F5) {
                    onFetchFilesClicked();
                }
            });
        });
    }

    // ================= SIDEBAR =================
    private void showSidebar() {
        if (sidebarVisible) return;

        sidebarVisible = true;

        TranslateTransition transition = new TranslateTransition(Duration.millis(110), sidebar);
        transition.setToX(180);
        transition.play();
    }

    private void hideSidebar() {
        if (!sidebarVisible || sidebarLocked) return;

        sidebarVisible = false;

        TranslateTransition transition = new TranslateTransition(Duration.millis(220), sidebar);
        transition.setToX(0);
        transition.play();
    }

    @FXML
    private void onSidebarLockClicked() {
        sidebarLocked = !sidebarLocked;

        if (sidebarLocked) {
            if (!sidebarLockButton.getStyleClass().contains("sidebar-icon-button-active")) {
                sidebarLockButton.getStyleClass().add("sidebar-icon-button-active");
            }
            showSidebar();
        } else {
            sidebarLockButton.getStyleClass().remove("sidebar-icon-button-active");
        }
    }

    // ================= FETCH FILES =================
    @FXML
    public void onFetchFilesClicked() {

        scannedPages = fileManager.processAndScanFiles();

        if (scannedPages == null || scannedPages.isEmpty()) {
            System.out.println("No files processed.");
            return;
        }

        fileListContainer.getChildren().clear();

        for (Page page : scannedPages) {
            addPageToUI(page);
        }
    }

    // ================= FILTER FILES =================
    private void filterFiles(String query) {

        if (scannedPages == null) return;

        String lowerQuery = query.trim().toLowerCase();

        fileListContainer.getChildren().clear();

        if (lowerQuery.isEmpty()) {
            for (Page page : scannedPages) {
                addPageToUI(page);
            }
            return;
        }

        for (Page page : scannedPages) {

            String name = page.getPageName() != null ? page.getPageName().toLowerCase() : "";
            String barcode = page.getBarcode() != null ? page.getBarcode().toLowerCase() : "";

            if (name.contains(lowerQuery) || barcode.contains(lowerQuery)) {
                addPageToUI(page);
            }
        }
    }

    // ================= ADD PAGE TO UI =================
    private void addPageToUI(Page page) {
        try {
            BufferedImage original = ImageIO.read(new File(page.getPagePath()));
            if (original == null) return;

            BufferedImage cropped = cropBackground(original);
            Image fxImage = SwingFXUtils.toFXImage(cropped, null);

            // Thumbnail container
            StackPane thumbContainer = new StackPane();
            thumbContainer.setPrefSize(120, 160);
            thumbContainer.setAlignment(Pos.CENTER);

            ImageView thumbnail = new ImageView(fxImage);
            thumbnail.setFitWidth(120);
            thumbnail.setFitHeight(160);
            thumbnail.setPreserveRatio(false);

            thumbContainer.getChildren().add(thumbnail);

            Button fileButton = new Button();
            fileButton.setGraphic(thumbContainer);
            fileButton.setText(page.getPageName());
            fileButton.setContentDisplay(ContentDisplay.TOP);
            fileButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

            fileButton.setOnAction(e -> {
                barcodeLabel.setText(page.getBarcode());
                previewImage.setImage(fxImage);
                previewImage.setPreserveRatio(true);
                previewImage.setFitWidth(500);
            });

            fileListContainer.getChildren().add(fileButton);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= SMART CROP =================
    private BufferedImage cropBackground(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int top = 0, bottom = height - 1;
        int left = 0, right = width - 1;

        for (int y = 0; y < height; y++) {
            boolean found = false;
            for (int x = 0; x < width; x++) {
                if (!isBackground(image.getRGB(x, y))) {
                    top = y;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        for (int y = height - 1; y >= 0; y--) {
            boolean found = false;
            for (int x = 0; x < width; x++) {
                if (!isBackground(image.getRGB(x, y))) {
                    bottom = y;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        for (int x = 0; x < width; x++) {
            boolean found = false;
            for (int y = 0; y < height; y++) {
                if (!isBackground(image.getRGB(x, y))) {
                    left = x;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        for (int x = width - 1; x >= 0; x--) {
            boolean found = false;
            for (int y = 0; y < height; y++) {
                if (!isBackground(image.getRGB(x, y))) {
                    right = x;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    // ================= LOGOUT =================
    @FXML
    private void onLogOutClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/gui/log-in.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BACKGROUND DETECTION =================
    private boolean isBackground(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        return (r > 200 && g > 200 && b > 200);
    }
}