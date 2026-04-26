package dk.easv.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

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
    private ImageView previewImage; //

    private boolean sidebarVisible = false;
    private boolean sidebarLocked = false;

    // ================= INIT =================
    @FXML
    public void initialize() {
        sidebarTrigger.setOnMouseEntered(e -> showSidebar());
        sidebar.setOnMouseExited(e -> hideSidebar());
        sidebarTrigger.toFront();

        //  F5 KEY LISTENER
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

        System.out.println("Fetched pages: " + scannedPages.size());

        fileListContainer.getChildren().clear();

        for (Page page : scannedPages) {

            String text = "     " + page.getPageName() + " (Ref: " + page.getDocumentId() + ")";

            if (page.getBarcode() != null && !page.getBarcode().equals("No barcode found.")) {
                text += " - " + page.getBarcode();
            }

            Button fileButton = new Button(text);
            fileButton.getStyleClass().add("file-row");

            // CLICK HANDLER (TIFF SUPPORT)
            fileButton.setOnAction(e -> {
                try {
                    // Update barcode
                    barcodeLabel.setText(page.getBarcode());

                    // Load TIFF image
                    BufferedImage bufferedImage = ImageIO.read(new File(page.getPagePath()));

                    if (bufferedImage != null) {
                        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        previewImage.setImage(fxImage);
                    } else {
                        System.out.println("Could not load image: " + page.getPagePath());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            fileListContainer.getChildren().add(fileButton);
        }
    }
}