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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import dk.easv.bll.FileManager;
import dk.easv.bll.TIFFService;
import dk.easv.be.Page;

import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

import java.util.List;

public class UserviewController {

    private final TIFFService apiService = new TIFFService();
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

    private boolean sidebarVisible = false;
    private boolean sidebarLocked = false;

    // ================= Initialize =================
    @FXML
    public void initialize() {
        sidebarTrigger.setOnMouseEntered(e -> showSidebar());
        sidebar.setOnMouseExited(e -> hideSidebar());
        sidebarTrigger.toFront();

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

            try {
                BufferedImage original = ImageIO.read(new File(page.getPagePath()));

                if (original == null) continue;

                //  Crop background to get better thumbnails and previews
                BufferedImage cropped = cropBackground(original);

                Image fxImage = SwingFXUtils.toFXImage(cropped, null);

                // ---------- THUMBNAIL ----------

                StackPane thumbContainer = new StackPane();
                thumbContainer.setPrefSize(120, 160);
                thumbContainer.setAlignment(Pos.CENTER);

                ImageView thumbnail = new ImageView(fxImage);
                thumbnail.setPreserveRatio(true);
                thumbnail.setSmooth(true);

                double imgW = fxImage.getWidth();
                double imgH = fxImage.getHeight();

                double boxW = 120;
                double boxH = 160;

                double scale = Math.max(boxW / imgW, boxH / imgH);

                thumbnail.setFitWidth(imgW * scale);
                thumbnail.setFitHeight(imgH * scale);

                // Clip overflow (this removes white borders visually)
                Rectangle clip = new Rectangle(120, 160);
                thumbContainer.setClip(clip);

                thumbContainer.getChildren().add(thumbnail);

                // Button
                Button fileButton = new Button();
                fileButton.setGraphic(thumbContainer);
                fileButton.setText(page.getPageName());
                fileButton.setContentDisplay(ContentDisplay.TOP);
                fileButton.getStyleClass().add("file-row");
                fileButton.setText(page.getPageName());
                fileButton.setContentDisplay(ContentDisplay.TOP);
                fileButton.getStyleClass().add("file-row");

                // ---------- CLICK ----------
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
    }

    // ================= SMART CROP =================
    private BufferedImage cropBackground(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int top = 0, bottom = height - 1;
        int left = 0, right = width - 1;

        // TOP
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isBackground(image.getRGB(x, y))) {
                    top = y;
                    break outer;
                }
            }
        }

        // BOTTOM
        outer:
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                if (!isBackground(image.getRGB(x, y))) {
                    bottom = y;
                    break outer;
                }
            }
        }

        // LEFT
        outer:
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!isBackground(image.getRGB(x, y))) {
                    left = x;
                    break outer;
                }
            }
        }

        // RIGHT
        outer:
        for (int x = width - 1; x >= 0; x--) {
            for (int y = 0; y < height; y++) {
                if (!isBackground(image.getRGB(x, y))) {
                    right = x;
                    break outer;
                }
            }
        }

        return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

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

        // Detect light colors (white + beige + light gray)
        return (r > 200 && g > 200 && b > 200);
    }
}