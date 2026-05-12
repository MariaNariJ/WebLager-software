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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import dk.easv.bll.FileManager;
import dk.easv.be.Page;
import dk.easv.be.DocumentGroup;

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

    private List<Page> scannedPages = new ArrayList<>();

    private List<DocumentGroup> documentGroups = new ArrayList<>();

    private boolean scanning = false;

    private int currentIndex = -1;

    private DocumentGroup activeDocument = null;

    private String selectedProfile = null;

    private String selectedBox = null;

    private DocumentGroup currentDocument;

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
    private TextField txtProfile;
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
    private Button btnSaveScan;

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

        for (int i = 0; i < scannedPages.size(); i++) {
            addPageToUI(scannedPages.get(i), i);
        }
    }

    private final Map<String, Runnable> keyBindings = new HashMap<>();

    // STATE
    private Image currentImage;

    // ZOOM
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private final double MIN_ZOOM = 0.2;
    private final double MAX_ZOOM = 4.0;

    private double rotationAngle = 0;

    private boolean sidebarVisible = true;
    private boolean sidebarLocked = true;

    // INITIALIZE
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

        //Locked autofilled fields
        txtProfile.setEditable(false);
        txtClient.setEditable(false);

        //Disabled at first
        btnSaveScan.setDisable(true);
        btnSaveScan.setOpacity(0.5);

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
        zoomLevel = 1.0;
        updateZoom();

        previewScrollPane.setHvalue(0.5);
        previewScrollPane.setVvalue(0.5);
    }

    private void updateZoom() {

        if (currentImage == null) return;

        previewImage.setImage(currentImage);
        previewImage.setPreserveRatio(true);

        double viewportWidth =
                previewScrollPane.getViewportBounds().getWidth();

        double viewportHeight =
                previewScrollPane.getViewportBounds().getHeight();

        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();

        double widthRatio = viewportWidth / imageWidth;
        double heightRatio = viewportHeight / imageHeight;

        double baseScale = Math.min(widthRatio, heightRatio);

        double finalScale = baseScale * zoomLevel;

        previewImage.setFitWidth(imageWidth * finalScale);
        previewImage.setFitHeight(imageHeight * finalScale);

        previewImage.setRotate(rotationAngle);

        StackPane container =
                (StackPane) previewScrollPane.getContent();

        container.setAlignment(Pos.CENTER);

        container.setMinWidth(viewportWidth);
        container.setMinHeight(viewportHeight);

        zoomLabel.setText((int)(zoomLevel * 100) + "%");
    }

    // ROTATION
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

    // NAVIGATION
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

    // SHOW PAGE
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

            // Restores rotation
            rotationAngle = page.getRotation();

            updateZoom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //FETCH
    @FXML
    public void onFetchFilesClicked() {

        // Prevent scanning without profile
        if (selectedProfile == null) {
            onSelectProfileClicked();
            return;
        }

        // Clear UI for next scan session
        fileListContainer.getChildren().clear();

        scannedPages = new ArrayList<>();

        currentIndex = -1;

        scanning = true;

        btnFetchFiles.setDisable(true);
        btnFetchFiles.setOpacity(0.6);

        CompletableFuture.runAsync(() -> {

            // REAL API VERSION - keep this for later
            // fileManager.proccesFilesFromApi(page -> {

            // LOCAL TEST BOX VERSION
            fileManager.scanNextDocument(page -> {

                Platform.runLater(() -> {

                    // Add page to UI
                    scannedPages.add(page);

                    addPageToUI(page, scannedPages.size() - 1);

                    fileCountLabel.setText(
                            String.valueOf(scannedPages.size())
                    );

                    // Check if page contains barcode
                    boolean hasBarcode =
                            page.getBarcode() != null &&
                                    !page.getBarcode().isBlank();

                    // FIRST barcode starts document
                    if (hasBarcode && currentDocument == null) {

                        currentDocument = new DocumentGroup(
                                "Document " + (documentGroups.size() + 1),
                                page.getBarcode()
                        );

                        currentDocument.addPage(page);

                        barcodeLabel.setText(
                                page.getBarcode()
                        );

                        return;
                    }

                    // SECOND barcode pauses scanning
                    if (hasBarcode && currentDocument != null) {

                        //documentGroups.add(currentDocument);
                        //renderDocumentOverview();

                        scanning = false;

                        // Disable scanning
                        btnFetchFiles.setDisable(true);
                        btnFetchFiles.setOpacity(0.5);

                        // Enable save button
                        btnSaveScan.setDisable(false);
                        btnSaveScan.setOpacity(1.0);

                        scanStatusLabel.setText(
                                "Document ready for metadata"
                        );

                        return;
                    }

                    // Normal pages added to document
                    if (currentDocument != null) {

                        currentDocument.addPage(page);
                    }

                    // Preview handling
                    if (currentIndex == -1) {

                        showPage(0);

                    } else {

                        updateViewingStatus();
                    }
                });
            });
            Platform.runLater(() -> {

                scanning = false;

                btnSaveScan.setDisable(false);
                btnSaveScan.setOpacity(1.0);

                scanStatusLabel.setText(
                        "Ready to save document"
                );
            });
        });
    }
    @FXML
    private void onSaveScanClicked() {

        finishCurrentDocument();

        // Lock previous scanned files
        for (javafx.scene.Node node : fileListContainer.getChildren()) {

            node.setDisable(true);

            if (!node.getStyleClass()
                    .contains("locked-file")) {

                node.getStyleClass().add("locked-file");
            }
        }

        btnSaveScan.setDisable(true);
        btnSaveScan.setOpacity(0.5);

        btnFetchFiles.setDisable(false);
        btnFetchFiles.setOpacity(1.0);

        scanStatusLabel.setText(
                "Ready for next document"
        );
    }
                // Old logic
        /*Platform.runLater(() -> {
                   // Group scanned pages into documents
                   documentGroups = fileManager.groupPagesIntoDocuments(scannedPages);

                   // Refresh document overview UI
                   renderDocumentOverview();

                   btnFetchFiles.setDisable(false);
                   btnFetchFiles.setOpacity(1.0);

                   scanning = false;
               });*/

    @FXML
    public void onSaveMetadataClicked() {
        if (scannedPages.isEmpty()) {
            return;
        }
        String Client = txtClient.getText();
        String BoxName = txtBox.getText();
        String DocumentName = txtDocumentName.getText();
        String Date = txtDate.getText();

        if (Client.isEmpty() || BoxName.isEmpty() || DocumentName.isEmpty() || Date.isEmpty()) {
            return;
        }

        Box box = new Box(BoxName, Client);
        int boxId = boxDAO.insertBox(box);
        Document document = new Document(
                boxId,
                scannedPages.getLast().getBarcode(),
                java.sql.Date.valueOf(Date),
                DocumentName,
                selectedProfile
        );
        int documentId = documentDAO.insertDocument(document);

        for (Page page : scannedPages) {
            page.setDocumentId(documentId);
            System.out.println("Saving page with barcode: " + page.getBarcode() + " linked to document ID: " + documentId);
            InputStream inputStream = fileManager.getFileStream(page);

            pageDAO.insertPage(page, inputStream);
        }
    }

    private void addPageToUI(Page page, int index) {
        try {

            Button btn = new Button();
            btn.setDisable(false);
            btn.getStyleClass().remove("locked-file");
            btn.setMnemonicParsing(false);
            btn.setText(page.getPageName());
            btn.getStyleClass().add("file-name-label");
            btn.getStyleClass().add("file-list-button");


            //IMAGE VIEW MODE
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

            //CLICK EVENT
            btn.setOnAction(e -> {

                if (page.getBarcode() == null) {
                    barcodeLabel.setText("No barcode found");
                } else {
                    barcodeLabel.setText(page.getBarcode());
                }

                showPage(index);
            });

            fileListContainer.getChildren().add(btn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // BACKGROUND DETECTION
    private boolean isBackground(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        return (r > 200 && g > 200 && b > 200);
    }

    //HELPER
    private double clamp(double value) {
        if (value < 0) return 0;
        if (value > 1) return 1;
        return value;
    }

    //IMAGE
    private BufferedImage cropBackground(BufferedImage image) {
        return image; // keep your original logic if needed
    }

    //FILTER
    private void filterFiles(String query) {

        if (scannedPages == null) return;

        String search = query == null ? "" : query.trim().toLowerCase();

        fileListContainer.getChildren().clear();

        // If empty it will show all
        if (search.isEmpty()) {

            for (int i = 0; i < scannedPages.size(); i++) {

                addPageToUI(scannedPages.get(i), i);
            }

            return;
        }

        for (int i = 0; i < scannedPages.size(); i++) {

            Page page = scannedPages.get(i);

            String fileName = page.getPageName() != null
                    ? page.getPageName().toLowerCase()
                    : "";

            String barcode = page.getBarcode() != null
                    ? page.getBarcode().toLowerCase()
                    : "";

            // partial match anywhere
            if (fileName.contains(search) || barcode.contains(search)) {

                addPageToUI(page, i);
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

    // LOGOUT
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

    /**
     * Opens/closes a document from Document Overview.
     */
    private void openDocument(DocumentGroup document) {

        // If same document is clicked again,
        // return to default scanning state
        if (activeDocument == document) {

            activeDocument = null;

            // Clear current pages
            scannedPages.clear();
            fileListContainer.getChildren().clear();

            // Reset preview image
            previewImage.setImage(null);

            // Clear barcode
            barcodeLabel.setText("");

            // Reset status text
            scanStatusLabel.setText("Ready for scanning");

            // Enable scanning again
            btnFetchFiles.setDisable(false);
            btnFetchFiles.setOpacity(1.0);

            currentIndex = -1;

            return;
        }

        // Open selected document
        activeDocument = document;

        fileListContainer.getChildren().clear();

        scannedPages = document.getPages();

        // Load pages into left sidebar
        for (int i = 0; i < scannedPages.size(); i++) {
            addPageToUI(scannedPages.get(i), i);
        }

        // Show first page automatically
        if (!scannedPages.isEmpty()) {
            showPage(0);
        }

        // Disable scanning while viewing existing document
        btnFetchFiles.setDisable(true);
        btnFetchFiles.setOpacity(0.5);

        updateViewingStatus();
    }

    private void finishCurrentDocument() {

        if (currentDocument == null) {
            return;
        }

        // Add finished document to overview
        documentGroups.add(currentDocument);

        renderDocumentOverview();

        // Reset active document
        currentDocument = null;

        // Enable scanning again
        btnFetchFiles.setDisable(false);
        btnFetchFiles.setOpacity(1.0);

        btnSaveScan.setDisable(true);
        btnSaveScan.setOpacity(0.5);

        scanning = false;

        scanStatusLabel.setText(
                "Ready for next document"
        );

        /*
        // If next barcode already exists,
        // create next document automatically
        if (pendingBarcode != null) {

            currentDocument = new DocumentGroup(
                    "Document " + (documentGroups.size() + 1),
                    pendingBarcode
            );

            pendingBarcode = null;
        }*/
    }

    /**
     * Displays grouped documents inside Document Overview.
     */
    private void renderDocumentOverview() {

            shelfContent.getChildren().clear();

        // Container holding all document cards
        javafx.scene.layout.HBox documentRow =
                new javafx.scene.layout.HBox(12);

        for (DocumentGroup document : documentGroups) {

            VBox card = new VBox(6);
            card.getStyleClass().add("mini-card");

            // Document title
            Label docTitle = new Label(document.getTitle());
            docTitle.getStyleClass().add("section-title");

            // Page count
            Label pages = new Label(
                    "Pages: " + document.getPages().size()
            );
            pages.getStyleClass().add("muted-text");

            // Barcode display
            Label barcode = new Label(
                    "Barcode: " + document.getBarcode()
            );
            barcode.getStyleClass().add("muted-text");

            card.getChildren().addAll(docTitle, pages, barcode);

            // Open selected document
            card.setOnMouseClicked(e -> openDocument(document));

            documentRow.getChildren().add(card);
        }

        // Add cards to overview
        shelfContent.getChildren().add(documentRow);
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

    @FXML
    private void onSelectProfileClicked() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("SelectProfile.fxml")
            );

            Parent root = loader.load();
            SelectProfileController controller =
                    loader.getController();

            controller.setUserviewController(this);

            Stage stage = new Stage();

            Scene scene = new Scene(root);

            scene.setFill(null);

            stage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);

            stage.setResizable(false);

            stage.show();

        } catch (Exception e) {

            System.out.println("POPUP ERROR");

            e.printStackTrace();
        }
    }

    public void setSelectedClient(String client) {

        txtClient.setText(client);

        // Optional lock field
        txtClient.setEditable(false);
    }

    public void setScanSetup(
            String boxId,
            String profile) {

        selectedBox = boxId;

        selectedProfile = profile;

        // Update UI fields
        txtBox.setText(boxId);

        txtProfile.setText(profile);
    }

}