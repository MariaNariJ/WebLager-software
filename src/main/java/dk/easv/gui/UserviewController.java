package dk.easv.gui;

import dk.easv.be.Box;
import dk.easv.be.Document;
import dk.easv.dal.dao.BoxDAO;
import dk.easv.dal.dao.DocumentDAO;
import dk.easv.dal.dao.PageDAO;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.StrictMath.clamp;

public class UserviewController {

    private final BoxDAO boxDAO = new BoxDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PageDAO pageDAO = new PageDAO();
    private final FileManager fileManager = new FileManager();
    private List<Page> scannedPages;
    private boolean scanning = false;

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
    private ScrollPane previewScrollPane;

    private void updateZoomLabel() {
        int percent = (int) (zoomLevel * 100);
        zoomLabel.setText(percent + "%");
    }

    private boolean sidebarVisible = true;
    private boolean sidebarLocked = true;


    // ================= ZOOM =================
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private final double MIN_ZOOM = 0.2;
    private final double MAX_ZOOM = 3.0;

    private Image currentImage;



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

        previewScrollPane.setOnScroll(event -> {
            if (currentImage == null) return;

            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;

            double oldZoom = zoomLevel;
            double newZoom = zoomLevel * zoomFactor;

            if (newZoom < MIN_ZOOM || newZoom > MAX_ZOOM) return;

            double mouseX = event.getX();
            double mouseY = event.getY();

            double hValue = previewScrollPane.getHvalue();
            double vValue = previewScrollPane.getVvalue();

            double contentWidth = previewImage.getBoundsInParent().getWidth();
            double contentHeight = previewImage.getBoundsInParent().getHeight();

            double viewportWidth = previewScrollPane.getViewportBounds().getWidth();
            double viewportHeight = previewScrollPane.getViewportBounds().getHeight();

            double mouseXRatio = (hValue * (contentWidth - viewportWidth) + mouseX) / contentWidth;
            double mouseYRatio = (vValue * (contentHeight - viewportHeight) + mouseY) / contentHeight;

            zoomLevel = newZoom;
            updateZoom();
            updateZoomLabel();

            double newContentWidth = contentWidth * (zoomLevel / oldZoom);
            double newContentHeight = contentHeight * (zoomLevel / oldZoom);

            double newHValue = (mouseXRatio * newContentWidth - mouseX) / (newContentWidth - viewportWidth);
            double newVValue = (mouseYRatio * newContentHeight - mouseY) / (newContentHeight - viewportHeight);

            previewScrollPane.setHvalue(clamp(newHValue));
            previewScrollPane.setVvalue(clamp(newVValue));

            event.consume();
        });

        previewImage.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                zoomLevel = 1.0;
                updateZoom();
                updateZoomLabel();
            }
        });

        Platform.runLater(() -> {
            sidebar.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.F5 && !scanning) {
                    onFetchFilesClicked();
                }
            });
        });
        // Sidebar locked and visible by default
        sidebar.setTranslateX(180);

        if (!sidebarLockButton.getStyleClass().contains("sidebar-icon-button-active")) {
            sidebarLockButton.getStyleClass().add("sidebar-icon-button-active");
        }
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

        TranslateTransition transition = new TranslateTransition(Duration.millis(110), sidebar);
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
        fileListContainer.getChildren().clear();
        scannedPages = new ArrayList<>();

        scanning = true;
        btnFetchFiles.setDisable(true);
        btnFetchFiles.setOpacity(0.6);

        CompletableFuture.runAsync(() -> {
            fileManager.proccesFilesFromApi(page -> {
                Platform.runLater(() -> {
                    scannedPages.add(page);
                    addPageToUI(page);
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
            fileButton.setMnemonicParsing(false);
            fileButton.setContentDisplay(ContentDisplay.TOP);
            fileButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

            fileButton.setOnAction(e -> {
                if (page.getBarcode() == null) {
                    barcodeLabel.setText("No barcode found");
                } else {
                    barcodeLabel.setText(page.getBarcode());
                }

                currentImage = fxImage;
                zoomLevel = 1.0;

                updateZoom();
                updateZoomLabel();
            });

            fileListContainer.getChildren().addFirst(fileButton);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= Zoom Logic =================

    private void updateZoom() {
        if (currentImage == null) return;

        previewImage.setImage(currentImage);
        previewImage.setPreserveRatio(true);

        // Base size (your UI size)
        double baseWidth = previewScrollPane.getWidth();
        double baseHeight = previewScrollPane.getHeight();

        previewImage.setFitWidth(baseWidth * zoomLevel);
        previewImage.setFitHeight(baseHeight * zoomLevel);
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

    // ================= HELPER =================
    private double clamp(double value) {
        if (value < 0) return 0;
        if (value > 1) return 1;
        return value;
    }
}