package dk.easv.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserviewController {

    private final FileManager fileManager = new FileManager();

    private List<Page> scannedPages;
    private boolean scanning = false;
    private int currentIndex = -1;

    private final Map<String, Runnable> keyBindings = new HashMap<>();

    @FXML private VBox fileListContainer;
    @FXML private VBox sidebar;
    @FXML private Pane sidebarTrigger;
    @FXML private Button sidebarLockButton;
    @FXML private Label barcodeLabel;
    @FXML private ImageView previewImage;
    @FXML private TextField searchField;
    @FXML private Label zoomLabel;
    @FXML private ScrollPane previewScrollPane;
    @FXML private Button btnFetchFiles;

    // ================= STATE =================
    private Image currentImage;

    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private final double MIN_ZOOM = 0.2;
    private final double MAX_ZOOM = 3.0;

    private double rotationAngle = 0;

    private boolean sidebarVisible = false;
    private boolean sidebarLocked = false;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        // Keybindings
        keyBindings.put("+", this::onZoomIn);
        keyBindings.put("-", this::onZoomOut);
        keyBindings.put("0", this::onResetZoom);
        keyBindings.put("d", this::onNextFile);
        keyBindings.put("a", this::onPreviousFile);
        keyBindings.put("q", this::onRotateLeft);
        keyBindings.put("e", this::onRotateRight);

        sidebarTrigger.setOnMouseEntered(e -> showSidebar());
        sidebar.setOnMouseExited(e -> hideSidebar());
        sidebarTrigger.toFront();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterFiles(newValue);
        });

        // CTRL + scroll zoom
        previewScrollPane.setOnScroll(event -> {
            if (!event.isControlDown()) return;
            if (currentImage == null) return;

            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            double newZoom = zoomLevel * zoomFactor;

            if (newZoom < MIN_ZOOM || newZoom > MAX_ZOOM) return;

            zoomLevel = newZoom;
            updateZoom();
            updateZoomLabel();

            event.consume();
        });

        // Double click reset
        previewImage.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onResetZoom();
            }
        });

        Platform.runLater(() -> {
            sidebar.getScene().setOnKeyPressed(event -> {

                if (event.getCode() == javafx.scene.input.KeyCode.F5 && !scanning) {
                    onFetchFilesClicked();
                    return;
                }

                String key = event.getText().toLowerCase();

                Runnable action = keyBindings.get(key);
                if (action != null) {
                    action.run();
                }
            });
        });
    }

    // ================= ZOOM =================
    @FXML
    private void onZoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel += ZOOM_STEP;
            updateZoom();
            updateZoomLabel();
        }
    }

    @FXML
    private void onZoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_STEP;
            updateZoom();
            updateZoomLabel();
        }
    }

    @FXML
    private void onResetZoom() {
        zoomLevel = 1.0;
        updateZoom();
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        zoomLabel.setText((int)(zoomLevel * 100) + "%");
    }

    private void updateZoom() {
        if (currentImage == null) return;

        previewImage.setImage(currentImage);
        previewImage.setPreserveRatio(true);

        double baseWidth = previewScrollPane.getWidth();
        double baseHeight = previewScrollPane.getHeight();

        previewImage.setFitWidth(baseWidth * zoomLevel);
        previewImage.setFitHeight(baseHeight * zoomLevel);

        previewImage.setRotate(rotationAngle);
    }

    // ================= ROTATION =================
    @FXML
    private void onRotateLeft() {
        rotationAngle -= 90;
        normalizeRotation();

        saveRotation();
        updateZoom();
    }

    @FXML
    private void onRotateRight() {
        rotationAngle += 90;
        normalizeRotation();

        saveRotation();
        updateZoom();
    }

    private void normalizeRotation() {
        rotationAngle = (rotationAngle % 360 + 360) % 360;
    }

    private void saveRotation() {
        if (currentIndex >= 0 && scannedPages != null) {
            Page page = scannedPages.get(currentIndex);
            page.setRotation((int) rotationAngle);

            // Persist to DB
            fileManager.updatePageRotation(page);
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void onNextFile() {
        if (scannedPages == null || scannedPages.isEmpty()) return;
        if (currentIndex < scannedPages.size() - 1) {
            showPage(currentIndex + 1);
        }
    }

    @FXML
    private void onPreviousFile() {
        if (scannedPages == null || scannedPages.isEmpty()) return;
        if (currentIndex > 0) {
            showPage(currentIndex - 1);
        }
    }

    // ================= SHOW PAGE =================
    private void showPage(int index) {
        if (scannedPages == null || scannedPages.isEmpty()) return;
        if (index < 0 || index >= scannedPages.size()) return;

        currentIndex = index;
        Page page = scannedPages.get(index);

        try {
            BufferedImage original = ImageIO.read(new File(page.getPagePath()));
            if (original == null) return;

            BufferedImage cropped = cropBackground(original);
            currentImage = SwingFXUtils.toFXImage(cropped, null);

            barcodeLabel.setText(page.getBarcode());

            zoomLevel = 1.0;

            // IMPORTANT: restore rotation
            rotationAngle = page.getRotation();

            updateZoom();
            updateZoomLabel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FETCH =================
    @FXML
    public void onFetchFilesClicked() {

        fileListContainer.getChildren().clear();
        scannedPages = new ArrayList<>();

        scanning = true;
        btnFetchFiles.setDisable(true);

        CompletableFuture.runAsync(() -> {
            fileManager.proccesFilesInOrder(page -> {
                Platform.runLater(() -> {
                    scannedPages.add(page);
                    addPageToUI(page);
                });
            });
        }).thenRun(() -> {
            Platform.runLater(() -> {
                btnFetchFiles.setDisable(false);
                scanning = false;
            });
        });
    }

    // ================= ADD PAGE =================
    private void addPageToUI(Page page) {
        try {
            BufferedImage img = ImageIO.read(new File(page.getPagePath()));
            if (img == null) return;

            Image fxImage = SwingFXUtils.toFXImage(img, null);

            ImageView thumbnail = new ImageView(fxImage);
            thumbnail.setFitWidth(120);
            thumbnail.setFitHeight(160);

            StackPane container = new StackPane(thumbnail);
            container.setAlignment(Pos.CENTER);

            Button btn = new Button(page.getPageName(), container);
            btn.setContentDisplay(ContentDisplay.TOP);

            int index = scannedPages.indexOf(page);

            btn.setOnAction(e -> showPage(index));

            fileListContainer.getChildren().addFirst(btn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FILTER =================
    private void filterFiles(String query) {
        if (scannedPages == null) return;

        fileListContainer.getChildren().clear();

        for (Page page : scannedPages) {
            if (page.getPageName().toLowerCase().contains(query.toLowerCase())) {
                addPageToUI(page);
            }
        }
    }

    // ================= SIDEBAR =================
    private void showSidebar() {
        if (sidebarVisible) return;
        sidebarVisible = true;

        new TranslateTransition(Duration.millis(110), sidebar)
                .setToX(180);
    }

    private void hideSidebar() {
        if (!sidebarVisible || sidebarLocked) return;
        sidebarVisible = false;

        new TranslateTransition(Duration.millis(110), sidebar)
                .setToX(0);
    }

    @FXML
    private void onSidebarLockClicked() {
        sidebarLocked = !sidebarLocked;
        if (sidebarLocked) showSidebar();
    }

    // ================= LOGOUT =================
    @FXML
    private void onLogOutClicked() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/dk/easv/gui/log-in.fxml"));
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= IMAGE =================
    private BufferedImage cropBackground(BufferedImage image) {
        return image; // keep your original logic if needed
    }
}