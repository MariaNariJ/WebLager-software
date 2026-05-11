package dk.easv.gui;

import dk.easv.be.Box;
import dk.easv.be.Document;
import dk.easv.dal.dao.BoxDAO;
import dk.easv.dal.dao.DocumentDAO;
import dk.easv.dal.dao.PageDAO;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;



public class UserviewController {

    private final BoxDAO boxDAO = new BoxDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PageDAO pageDAO = new PageDAO();
    private final FileManager fileManager = new FileManager();
    private List<Page> scannedPages;
    private boolean scanning = false;
    private int currentIndex = -1;

    @FXML
    private Label fileCountLabel;
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
    @FXML
    private Label zoomLabel;
    @FXML
    private Button btnFetchFiles;
    @FXML
    private TextField txtClient;
    @FXML
    private TextField txtBox;
    @FXML
    private TextField txtDocumentName;
    @FXML
    private TextField txtDocumentType;
    @FXML
    private TextField txtDate;
    @FXML
    private ScrollPane previewScrollPane;
    @FXML
    private Button listViewButton;
    @FXML
    private Button imageViewButton;
    private boolean imageViewMode = false;
    @FXML
    private Label scanStatusLabel;
    @FXML
    private VBox shelfContent;
    @FXML
    private Label shelfArrow;
    private boolean shelfOpen = true;
    @FXML
    private VBox mainContent;

    @FXML
    private void onListViewClicked() {
        imageViewMode = false;

        listViewButton.getStyleClass().remove("view-toggle-button");
        listViewButton.getStyleClass().remove("view-toggle-button-active");
        listViewButton.getStyleClass().add("view-toggle-button-active");

        imageViewButton.getStyleClass().remove("view-toggle-button");
        imageViewButton.getStyleClass().remove("view-toggle-button-active");
        imageViewButton.getStyleClass().add("view-toggle-button");

        refreshFileList();
    }

    @FXML
    private void onImageViewClicked() {
        imageViewMode = true;

        imageViewButton.getStyleClass().remove("view-toggle-button");
        imageViewButton.getStyleClass().remove("view-toggle-button-active");
        imageViewButton.getStyleClass().add("view-toggle-button-active");

        listViewButton.getStyleClass().remove("view-toggle-button");
        listViewButton.getStyleClass().remove("view-toggle-button-active");
        listViewButton.getStyleClass().add("view-toggle-button");

        refreshFileList();
    }

    private void refreshFileList() {
        if (scannedPages == null) return;

        fileListContainer.getChildren().clear();

        for (Page page : scannedPages) {
            addPageToUI(page);
        }
    }

    private final Map<String, Runnable> keyBindings = new HashMap<>();

    // ================= STATE =================
    private Image currentImage;

    // ================= ZOOM =================
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private final double MIN_ZOOM = 0.2;
    private final double MAX_ZOOM = 4.0;

    private double rotationAngle = 0;

    private boolean sidebarVisible = true;
    private boolean sidebarLocked = true;

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
        sidebar.toFront();
        sidebarLockButton.setPickOnBounds(true);
        sidebarLockButton.toFront();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {filterFiles(newValue);
        });

        zoomSetup();

        // Double-click reset
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
        // Sidebar locked and visible by default
        sidebar.setTranslateX(180);

        if (!sidebarLockButton.getStyleClass().contains("sidebar-icon-button-active")) {
            sidebarLockButton.getStyleClass().add("sidebar-icon-button-active");
        }

        // Bottom shelf starts collapsed
        shelfOpen = false;
        shelfContent.setVisible(false);
        shelfContent.setManaged(false);
        shelfArrow.setRotate(0); // ▲ = open

    }

    private void zoomSetup() {
        previewScrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown() || currentImage == null) return;
            event.consume();

            double oldZoom = zoomLevel;
            zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel * (event.getDeltaY() > 0 ? 1.1 : 0.9)));
            if (oldZoom == zoomLevel) return;

            double f = zoomLevel / oldZoom;
            Bounds view = previewScrollPane.getViewportBounds();
            double contentW = previewScrollPane.getContent().getBoundsInParent().getWidth();
            double contentH = previewScrollPane.getContent().getBoundsInParent().getHeight();

            double viewCenterX = view.getWidth() / 2.0;
            double viewCenterY = view.getHeight() / 2.0;

            double targetX = (previewScrollPane.getHvalue() * Math.max(0, contentW - view.getWidth())) + viewCenterX;
            double targetY = (previewScrollPane.getVvalue() * Math.max(0, contentH - view.getHeight())) + viewCenterY;

            updateZoom();

            previewScrollPane.applyCss();
            previewScrollPane.layout();

            double newW = previewScrollPane.getContent().getBoundsInParent().getWidth();
            double newH = previewScrollPane.getContent().getBoundsInParent().getHeight();

            double newHval = ((targetX * f) - viewCenterX) / Math.max(1, newW - view.getWidth());
            double newVval = ((targetY * f) - viewCenterY) / Math.max(1, newH - view.getHeight());

            previewScrollPane.setHvalue(Math.max(0, Math.min(1, newHval)));
            previewScrollPane.setVvalue(Math.max(0, Math.min(1, newVval)));
        });
    }

    @FXML
    private void onZoomIn() {
        if (zoomLevel <= MAX_ZOOM) {
            zoomLevel += ZOOM_STEP;
            updateZoom();
        }
    }

    @FXML
    private void onZoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_STEP;
            updateZoom();
        }
    }

    @FXML
    private void onResetZoom() {
        zoomLevel = 0.7;
        updateZoom();

        previewScrollPane.setHvalue(0.5);
        previewScrollPane.setVvalue(0.5);
    }

    private void updateZoom() {
        if (currentImage == null) return;

        previewImage.setImage(currentImage);
        previewImage.setPreserveRatio(true);

        StackPane imageContainer = (StackPane) previewScrollPane.getContent();
        imageContainer.setScaleX(1);
        imageContainer.setScaleY(1);

        double baseWidth = previewScrollPane.getViewportBounds().getWidth();

        previewImage.setFitWidth(baseWidth * zoomLevel);

        previewImage.setRotate(rotationAngle);
        zoomLabel.setText((int)(zoomLevel * 100) + "%");
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

            zoomLevel = 0.7;

            // IMPORTANT: restore rotation
            rotationAngle = page.getRotation();

            updateZoom();
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
        btnFetchFiles.setOpacity(0.6);

        CompletableFuture.runAsync(() -> {
            // REAL API VERSION - keep this for later
            // fileManager.proccesFilesFromApi(page -> {

            // LOCAL TEST BOX VERSION
            fileManager.processFilesFromLocalBox(page -> {
                Platform.runLater(() -> {
                    scannedPages.add(page);
                    addPageToUI(page);

                    fileCountLabel.setText(String.valueOf(scannedPages.size()));

                    if (currentIndex == -1) {
                        showPage(0);
                    } else {
                        updateViewingStatus();
                    }
                });
            });
        }).thenRun(() -> {
            Platform.runLater(() -> {
                btnFetchFiles.setDisable(false);
                btnFetchFiles.setOpacity(1.0);
                scanning = false;
            });
        });
    }

    @FXML
    public void onSaveMetadataClicked() {
        if (scannedPages.isEmpty()) {
            return;
        }
        String Client = txtClient.getText();
        String BoxName = txtBox.getText();
        String DocumentName = txtDocumentName.getText();
        String DocumentType = txtDocumentType.getText();
        String Date = txtDate.getText();

        if (Client.isEmpty() || BoxName.isEmpty() || DocumentName.isEmpty() || Date.isEmpty()) {
            return;
        }

        Box box = new Box(BoxName, Client);
        int boxId = boxDAO.insertBox(box);
        Document document = new Document(boxId, scannedPages.getLast().getBarcode(), java.sql.Date.valueOf(Date), DocumentName, DocumentType);
        int documentId = documentDAO.insertDocument(document);

        for (Page page : scannedPages) {
            page.setDocumentId(documentId);
            System.out.println("Saving page with barcode: " + page.getBarcode() + " linked to document ID: " + documentId);
            InputStream inputStream = fileManager.getFileStream(page);

            pageDAO.insertPage(page, inputStream);
        }
    }

    private void addPageToUI(Page page) {
        try {

            Button btn = new Button();
            btn.setMnemonicParsing(false);
            btn.setText(page.getPageName());

            btn.getStyleClass().add("file-name-label");

            int index = scannedPages.indexOf(page);

            // IMAGE VIEW MODE
            if (imageViewMode) {

                BufferedImage img;

                try {
                    img = ImageIO.read(new File(page.getPagePath()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                if (img == null) return;

                BufferedImage processed = cropBackground(img);

                Image fxImage = SwingFXUtils.toFXImage(processed, null);

                ImageView thumbnail = new ImageView(fxImage);

                thumbnail.setFitWidth(120);
                thumbnail.setFitHeight(160);
                thumbnail.setPreserveRatio(true);

                StackPane container = new StackPane(thumbnail);
                container.setAlignment(Pos.CENTER);

                btn.setGraphic(container);
                btn.setContentDisplay(ContentDisplay.TOP);

            } else {

                btn.setGraphic(null);
                btn.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            //BUTTON STYLING
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-padding: 4 0 4 0;"
            );

            // ================= CLICK EVENT =================
            btn.setOnAction(e -> {

            fileListContainer.getChildren().add(btn);
                if (page.getBarcode() == null) {
                    barcodeLabel.setText("No barcode found");
                } else {
                    barcodeLabel.setText(page.getBarcode());
                }

                showPage(index);
            });

            fileListContainer.getChildren().add(0, btn);

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

    // ================= HELPER =================
    private double clamp(double value) {
        if (value < 0) return 0;
        if (value > 1) return 1;
        return value;
    }

    // ================= IMAGE =================
    private BufferedImage cropBackground(BufferedImage image) {
        return image; // keep your original logic if needed
    }

    // ================= FILTER =================
    private void filterFiles(String query) {

        if (scannedPages == null) return;

        String search = query == null ? "" : query.trim().toLowerCase();

        fileListContainer.getChildren().clear();

        // If empty it will show all
        if (search.isEmpty()) {
            for (Page page : scannedPages) {
                addPageToUI(page);
            }
            return;
        }

        for (Page page : scannedPages) {

            String fileName = page.getPageName() != null
                    ? page.getPageName().toLowerCase()
                    : "";

            String barcode = page.getBarcode() != null
                    ? page.getBarcode().toLowerCase()
                    : "";

            // partial match anywhere
            if (fileName.contains(search) || barcode.contains(search)) {
                addPageToUI(page);
            }
        }
    }


    private void showSidebar() {
        if (sidebarVisible) return;

        sidebarVisible = true;
        sidebar.toFront();

        TranslateTransition transition = new TranslateTransition(Duration.millis(110), sidebar);
        transition.setToX(180);
        transition.play();
    }

    private void hideSidebar() {
        if (!sidebarVisible || sidebarLocked) return;

        sidebarVisible = false;

        TranslateTransition transition = new TranslateTransition(Duration.millis(110), sidebar);
        transition.setToX(0);
        transition.play();
    }

    @FXML
    private void onSidebarLockClicked() {
        sidebarLocked = !sidebarLocked;

        if (sidebarLocked) {
            sidebarLockButton.getStyleClass().add("sidebar-icon-button-active");

            AnchorPane.setLeftAnchor(mainContent, 187.0);

            sidebarVisible = true;
            sidebar.setTranslateX(180);
            sidebar.toFront();

        } else {
            sidebarLockButton.getStyleClass().remove("sidebar-icon-button-active");

            AnchorPane.setLeftAnchor(mainContent, 8.0);

            sidebarVisible = false;
            sidebar.setTranslateX(0);
        }
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

    public void onFitToWidth(ActionEvent actionEvent) {
    }

    private void updateViewingStatus() {

        if (scannedPages == null || scannedPages.isEmpty()) {
            scanStatusLabel.setText("No files loaded");
            return;
        }

        int currentPage = currentIndex + 1;
        int totalPages = scannedPages.size();

        scanStatusLabel.setText(
                "Viewing page " + currentPage +
                        " out of " + totalPages
        );
    }

    // Bottom shelf
    @FXML
    private void toggleBottomShelf() {

        shelfOpen = !shelfOpen;

        shelfContent.setVisible(shelfOpen);
        shelfContent.setManaged(shelfOpen);

        RotateTransition rt = new RotateTransition(Duration.millis(200), shelfArrow);

        // Rotate relative instead of absolute
        rt.setByAngle(shelfOpen ? -180 : 180);

        rt.play();
    }

}