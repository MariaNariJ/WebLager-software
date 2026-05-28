package dk.easv.gui;

import dk.easv.be.*;
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
import javafx.scene.layout.*;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Label;
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
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import dk.easv.bll.DocumentManager;
import dk.easv.bll.LogManager;


public class UserviewController {
    private final FileManager fileManager = new FileManager();
    private final DocumentManager documentManager = new DocumentManager();
    private final LogManager logManager = new LogManager();

    private List<Page> scannedPages = new ArrayList<>();

    private List<DocumentGroup> documentGroups = new ArrayList<>();

    private boolean scanning = false;

    private boolean scanningFinished = false;

    private int currentIndex = -1;

    private String selectedProfile = null;

    private String selectedBox = null;

    private DocumentGroup currentDocument;

    private final List<DocumentGroup> scannedDocuments = new ArrayList<>();

    @FXML
    private Label fileCountLabel;
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
    private StackPane shortcutOverlay;
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
    private VBox mainContent;
    @FXML
    private Button btnSaveasDocument;
    @FXML
    private Button btnSaveScan;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Button scanningButton;
    @FXML
    private Button btnSendToExport;
    @FXML
    private Button btnFinishBox;
    @FXML
    private Label documentStatusLabel;
    @FXML
    private Label fileIndicatorLabel;
    @FXML
    private Button exportButton;
    @FXML
    private TreeView<String> documentTreeView;
    @FXML
    private AnchorPane appRoot;
    @FXML
    private ImageView sidebarLogoImage;
    @FXML
    private Button themeToggleButton;

    private boolean lightMode = false;
    private boolean shortcutVisible = false;
    private static final String DARK_CSS = "/dk/easv/gui/css/app.css";
    private static final String LIGHT_CSS = "/dk/easv/gui/css/lightmode.css";
    private int documentCounter = 1;
    private final Map<String, Page> pageMap = new HashMap<>();


    @FXML
    private void onThemeToggleClicked() {
        lightMode = !lightMode;

        String cssFile = lightMode ? LIGHT_CSS : DARK_CSS;

        String css = getClass()
                .getResource(cssFile)
                .toExternalForm();

        appRoot.getStylesheets().clear();
        appRoot.getStylesheets().add(css);

        themeToggleButton.setText(lightMode ? "☾" : "☀");
        sidebarLogoImage.setImage(new Image(
                getClass().getResourceAsStream(
                        lightMode
                                ? "/dk/easv/gui/assets/LogoBlue_Logoicon.png"
                                : "/dk/easv/gui/assets/Logo_DarkMode_Blue.png"
                )
        ));
    }


    private final List<javafx.scene.Node> scanningView = new ArrayList<>();

    private User loggedInUser
            ;
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

    private void toggleShortcutOverlay() {

        shortcutVisible = !shortcutVisible;

        shortcutOverlay.setVisible(shortcutVisible);
        shortcutOverlay.setManaged(shortcutVisible);
    }

    private void refreshFileList() {
        if (scannedPages == null) return;

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
    private final double GRANULAR_ROTATION_STEP = 1;

    private boolean sidebarVisible = true;
    private boolean sidebarLocked = true;

    // INITIALIZE
    @FXML
    public void initialize() {

        // Keybindings
        keyBindings.put("+", this::onZoomIn);
        keyBindings.put("-", this::onZoomOut);
        keyBindings.put("0", this::onResetZoom);

        keyBindings.put("q", this::onRotateLeft);
        keyBindings.put("e", this::onRotateRight);
        keyBindings.put("r", this::onResetRotation);

        keyBindings.put("i", this::toggleShortcutOverlay);

        keyBindings.put("f", this::onFitToWidth);

        keyBindings.put("d", this::onNextFile);
        keyBindings.put("a", this::onPreviousFile);

        keyBindings.put("s", this::onSaveasDocumentClicked);

        keyBindings.put("b", this::onFinishBoxClicked);

        keyBindings.put("l", this::onSidebarLockClicked);

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
        txtDate.setEditable(false);


        //Disabled at first
        btnSaveasDocument.setDisable(true);
        btnSaveasDocument.setOpacity(0.5);

        btnSendToExport.setDisable(true);
        btnSendToExport.getStyleClass().add("disabled-action-button");

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

                // Granular rotation with CTRL
                if (event.isControlDown()) {

                    // ROTATION SHORTCUTS

                    if (key.equals("q")) {
                        rotateGranularLeft();
                        return;
                    }

                    if (key.equals("e")) {
                        rotateGranularRight();
                        return;
                    }

                    // TIFF REORDER SHORTCUTS

                    if (event.getCode() == javafx.scene.input.KeyCode.UP) {
                        moveSelectedFileUp();
                        return;
                    }

                    if (event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                        moveSelectedFileDown();
                        return;
                    }
                }

                // Export SHORTCUT

                if (event.isShiftDown()) {

                    if (key.equals("q")) {

                        onSendToExportClicked();

                        return;
                    }
                }

                // NORMAL TIFF NAVIGATION

                if (event.getCode() == javafx.scene.input.KeyCode.UP) {
                    onPreviousFile();
                    return;
                }

                if (event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                    onNextFile();
                    return;
                }
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

        documentTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null) return;

                    String selected = newItem.getValue();

                    Page selectedPage = pageMap.get(selected);

                    if (selectedPage != null) {

                        showSpecificPage(selectedPage);
                    }
                });
        btnSendToExport.setDisable(true);

        scanningView.addAll(mainContent.getChildren());
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

        updateRotationIndicator();

        saveRotation();

        updateZoom();
    }

    @FXML
    private void onRotateRight() {

        rotationAngle += 90;

        normalizeRotation();

        updateRotationIndicator();

        saveRotation();

        updateZoom();
    }

    @FXML
    private void onResetRotation() {

        rotationAngle = 0;

        updateRotationIndicator();

        saveRotation();

        updateZoom();
    }

    @FXML
    private void rotateGranularLeft() {

        rotationAngle -= GRANULAR_ROTATION_STEP;

        normalizeRotation();

        updateRotationIndicator();

        saveRotation();

        updateZoom();
    }

    @FXML
    private void rotateGranularRight() {

        rotationAngle += GRANULAR_ROTATION_STEP;

        normalizeRotation();

        updateRotationIndicator();

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

    @FXML
    private Label rotationIndicator;

    private void updateRotationIndicator() {

        double displayAngle = rotationAngle;

        if (displayAngle > 180) {
            displayAngle -= 360;
        }

        rotationIndicator.setText((int) displayAngle + "°");
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



    @FXML
    private void moveSelectedFileUp() {

        if (scannedPages == null || scannedPages.isEmpty()) return;

        if (currentIndex <= 0) return;

        Collections.swap(scannedPages, currentIndex, currentIndex - 1);

        currentIndex--;

        refreshFileList();

        showPage(currentIndex);
    }

    @FXML
    private void moveSelectedFileDown() {

        if (scannedPages == null || scannedPages.isEmpty()) return;

        if (currentIndex >= scannedPages.size() - 1) return;

        Collections.swap(scannedPages, currentIndex, currentIndex + 1);

        currentIndex++;

        refreshFileList();

        showPage(currentIndex);
    }

    // SHOW PAGE
    private void showPage(int index) {
        if (scannedPages == null || scannedPages.isEmpty()) return;
        if (index < 0 || index >= scannedPages.size()) return;

        currentIndex = index;
        Page page = scannedPages.get(index);
        fileIndicatorLabel.setText(
                "File " +
                        (currentIndex + 1) +
                        " of " +
                        scannedPages.size()
        );
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

        if (currentDocument != null) {

            return;
        }

        // Prevent scanning without profile
        if (selectedProfile == null) {
            onSelectProfileClicked();
            return;
        }

        // Clear UI for next scan session
        scannedPages = new ArrayList<>();

        currentIndex = -1;
        scanning = true;

        btnFetchFiles.setDisable(true);
        btnSendToExport.setDisable(true);

        btnFetchFiles.setOpacity(0.6);

        CompletableFuture.runAsync(() -> {

            // REAL API VERSION - keep this for later
            // fileManager.proccesFilesFromApi(page -> {

            // LOCAL TEST BOX VERSION
            fileManager.scanNextDocument(page -> {

                Platform.runLater(() -> {

                    // Add page to UI
                    scannedPages.add(page);

                    refreshDocumentTree();

                    //addPageToUI(page, scannedPages.size() - 1);

                    fileCountLabel.setText(
                            String.valueOf(scannedPages.size())
                    );

                    // Check if page contains barcode
                    boolean hasBarcode =
                            page.getBarcode() != null &&
                                    !page.getBarcode().isBlank();

                    // FIRST barcode starts document
                    if (hasBarcode && currentDocument == null) {

                        // Autofill suggested document name
                        txtDocumentName.clear();

                        txtDocumentName.setPromptText(
                                "Document " + (documentGroups.size() + 1)
                        );
                        documentStatusLabel.setText("Ready for scanning");

                        // Update status
                        documentStatusLabel.setText("Scanning in progress");
                        scanStatusLabel.setText("Scanning in progress");

                        currentDocument = new DocumentGroup(
                                "Document " + documentCounter,
                                page.getBarcode()
                        );


                        currentDocument.addPage(page);

                        barcodeLabel.setText(
                                page.getBarcode()

                        );
                        // Autofill document information
                        txtDocumentName.clear();

                        txtDate.setText(
                                java.time.LocalDate.now()
                                        .format(
                                                java.time.format.DateTimeFormatter
                                                        .ofPattern("dd-MM-yyyy")
                                        )
                        );

                        return;
                    }

                    // SECOND barcode pauses scanning
                    // Barcode page inside current document
                    if (hasBarcode && currentDocument != null) {

                        currentDocument.addPage(page);

                        refreshDocumentTree();

                        return;
                    }

                    // Normal pages added to document
                    if (currentDocument != null) {

                        currentDocument.addPage(page);

                        refreshDocumentTree();
                    }

                });
            });
            Platform.runLater(() -> {

                scanning = false;

                btnSaveasDocument.setDisable(false);
                btnSaveasDocument.setOpacity(1.0);

                scanStatusLabel.setText(
                        "Ready to save document"
                );
                if (!scannedPages.isEmpty()) {

                    showPage(0);
                }
            });
        });
    }
    @FXML
    private void onSaveasDocumentClicked() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("SaveDocument.fxml")
            );

            Parent root = loader.load();

            SaveDocumentController controller =
                    loader.getController();

            controller.setUserviewController(this);

            Stage stage = new Stage();

            Scene scene = new Scene(root);

            stage.initStyle(StageStyle.TRANSPARENT);

            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);

            // Get button position on screen
            Bounds bounds = btnSaveasDocument.localToScreen(
                    btnSaveasDocument.getBoundsInLocal()
            );

            // Position popup above Save as Document button
            double x = bounds.getMinX() - 110;
            double y = bounds.getMinY() - 260;

            stage.setX(x);
            stage.setY(y);

            stage.setResizable(false);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
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

    // TODO: rename to onSendToExportClicked()
    @FXML
    public void onSendToExportClicked() {

        if (documentGroups.isEmpty()) {
            return;
        }

        String client = txtClient.getText();
        String boxName = txtBox.getText();
        String documentName = txtDocumentName.getText();

        if (documentName == null || documentName.trim().isEmpty()) {

            documentName =
                    txtDocumentName.getPromptText();
        }
        final String finalDocumentName = documentName;
        String date = txtDate.getText();

        if (client.isEmpty() ||
                boxName.isEmpty() ||
                documentName.isEmpty() ||
                date.isEmpty()) {

            return;
        }

        btnFetchFiles.setDisable(false);
        btnFetchFiles.setOpacity(1.0);

        scanStatusLabel.setText(
                "Sending box to Export..."
        );

        CompletableFuture.runAsync(() -> {

            // QA flow removed - now used for export pipeline
            documentManager.saveBoxForExport(
                    documentGroups,
                    txtClient.getText(),
                    txtBox.getText(),
                    selectedProfile,
                    txtDate.getText()
            );

            createLog(
                    "Info",
                    "Box Sent To Export",
                    "Box " + txtBox.getText() + " was sent to Export.",
                    "Completed"
            );


            // UI updates
            Platform.runLater(() -> {

                scanStatusLabel.setText(
                        "Sent to Export"
                );

                documentStatusLabel.setText(
                        "Ready for Export"
                );

                resetScanningSession();

                loadUserTab("user-export.fxml");

                setInactiveUserTab(scanningButton);
                setActiveUserTab(exportButton);

                btnSaveasDocument.setDisable(true);

                btnSaveasDocument.setOpacity(0.5);

                btnFetchFiles.setDisable(false);
                btnFetchFiles.setOpacity(1.0);

                scanning = false;

                // Update button state
                btnSendToExport.setDisable(true);
            });
        });
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

    //Sidebar
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

    @FXML
    public void onFitToWidth(ActionEvent actionEvent) {
        fitToWidth();
    }

    private void onFitToWidth() {
        fitToWidth();
    }

    private void fitToWidth() {

        if (currentImage == null) return;

        double viewportWidth =
                previewScrollPane.getViewportBounds().getWidth();

        double imageWidth = currentImage.getWidth();

        zoomLevel = viewportWidth / imageWidth;

        updateZoom();
    }

    private void finishCurrentDocument() {

        if (currentDocument == null) {
            return;
        }

        // Add finished document to overview
        documentGroups.add(currentDocument);

        currentDocument = null;

        refreshDocumentTree();

        btnSaveasDocument.setDisable(true);
        btnSaveasDocument.setOpacity(0.5);

        // Enable next scan
        btnFetchFiles.setDisable(false);
        btnFetchFiles.setOpacity(1.0);

        scanStatusLabel.setText(
                "Ready for next scan"
        );

        documentCounter++;
        currentDocument = null;
    }

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

    /**
     * Displays grouped documents inside Document Overview.
     */

    @FXML
    private void onSelectProfileClicked() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("SelectProfile.fxml")
            );

            Parent root = loader.load();

            root.setStyle(
                    "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;"
            );

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
            String client,
            String boxId,
            String profile) {

        selectedBox = boxId;
        selectedProfile = profile;

        txtClient.setText(client);
        txtBox.setText(boxId);
        txtProfile.setText(profile);
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;

        userNameLabel.setText(user.getName());
        userRoleLabel.setText(user.getRole());
    }
    @FXML
    private void onScanningClicked() {

        mainContent.getChildren().setAll(scanningView);

        setActiveUserTab(scanningButton);
        setInactiveUserTab(exportButton);
    }

    @FXML
    private void onExportClicked() {

        loadUserTab("user-export.fxml");

        setInactiveUserTab(scanningButton);
        setActiveUserTab(exportButton);
    }

    private void loadUserTab(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/dk/easv/gui/" + fxmlFile)
            );

            mainContent.getChildren().setAll(root);

            VBox.setVgrow(root, Priority.ALWAYS);

            if (root instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveUserTab(Button button) {
        button.getStyleClass().remove("nav-button");

        if (!button.getStyleClass().contains("nav-button-active")) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    private void setInactiveUserTab(Button button) {
        button.getStyleClass().remove("nav-button-active");

        if (!button.getStyleClass().contains("nav-button")) {
            button.getStyleClass().add("nav-button");
        }
    }

    // Returns how many documents currently exist
    // Used for automatic default naming
    public int getDocumentCount() {

        return documentGroups.size();
    }

    // Saves the currently scanned document
    public void saveCurrentDocument(String documentName) {

        // Safety check
        if (currentDocument == null) {
            return;
        }

        // Apply chosen document name
        if (documentName == null || documentName.trim().isEmpty()) {
            documentName =
                    "Document " + (documentGroups.size() + 1);
        }
        currentDocument.setTitle(documentName);

        // Update Document Information panel
        txtDocumentName.setText(documentName);

        // Finalize document and add to overview
        finishCurrentDocument();

        createLog(
                "Info",
                "Document Saved",
                "Saved document: " + documentName,
                "Completed"
        );

        // Reset Export button state
        btnSendToExport.setText("Send to Export");

        if (!scanningFinished) {
            btnSendToExport.setDisable(true);
        }

        // Update status
        scanStatusLabel.setText("Ready for next scan");
    }

    private void refreshDocumentTree() {

        pageMap.clear();

        String boxName = txtBox.getText();

        TreeItem<String> root =
                new TreeItem<>(boxName);

        root.setExpanded(true);

        // Saved documents
        for (DocumentGroup document : documentGroups) {

            TreeItem<String> documentNode =
                    new TreeItem<>(document.getTitle());

            documentNode.setExpanded(false);

            for (Page page : document.getPages()) {

                pageMap.put(page.getPageName(), page);

                TreeItem<String> pageNode =
                        new TreeItem<>(page.getPageName());

                documentNode.getChildren().add(pageNode);
            }
            root.getChildren().add(documentNode);
        }

        // Current active document
        if (currentDocument != null) {

            TreeItem<String> currentNode =
                    new TreeItem<>(currentDocument.getTitle());

            currentNode.setExpanded(true);

            for (Page page : currentDocument.getPages()) {

                pageMap.put(page.getPageName(), page);

                TreeItem<String> pageNode =
                        new TreeItem<>(page.getPageName());

                currentNode.getChildren().add(pageNode);
            }

            root.getChildren().add(currentNode);
        }

        documentTreeView.setRoot(root);

        documentTreeView.setShowRoot(true);
    }

    @FXML
    private void onFinishBoxClicked() {

        scanningFinished = true;
        btnSendToExport.setDisable(false);
        btnSendToExport.getStyleClass().remove("disabled-action-button");

        scanStatusLabel.setText("Box ready for Export");
        documentStatusLabel.setText(
                "Scanning completed"
        );

        btnFinishBox.setDisable(true);

        createLog(
                "Info",
                "Box Finished",
                "Box " + txtBox.getText() + " marked as ready for Export",
                "Completed"
        );
    }

    private void showSpecificPage(Page page) {

        try {

            BufferedImage original =
                    ImageIO.read(new File(page.getPagePath()));
            if (original == null) return;

            BufferedImage cropped = cropBackground(original);

            currentImage =
                    SwingFXUtils.toFXImage(cropped, null);

            previewImage.setImage(currentImage);
            barcodeLabel.setText(page.getBarcode());
            rotationAngle = page.getRotation();
            updateZoom();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLog(String level, String event, String details, String status) {
        Integer userId = loggedInUser != null ? loggedInUser.getId() : null;

        logManager.createLog(
                level,
                "Scanning",
                event,
                userId,
                details,
                status,
                "00:00:00"
        );
    }

    private void resetScanningSession() {

        documentGroups.clear();
        scannedDocuments.clear();
        scannedPages.clear();
        pageMap.clear();

        currentDocument = null;
        currentImage = null;

        selectedProfile = null;
        selectedBox = null;

        currentIndex = -1;
        documentCounter = 1;

        scanning = false;
        scanningFinished = false;

        rotationAngle = 0;
        updateRotationIndicator();

        zoomLevel = 1.0;
        zoomLabel.setText("100%");

        documentTreeView.setRoot(null);
        previewImage.setImage(null);

        barcodeLabel.setText("No barcode found");
        fileCountLabel.setText("0");
        fileIndicatorLabel.setText("No file selected");

        scanStatusLabel.setText("Ready for scanning");
        documentStatusLabel.setText("Ready for scanning");

        txtClient.clear();
        txtBox.clear();
        txtDate.clear();

        txtProfile.clear();

        txtDocumentName.clear();
        txtDocumentName.setPromptText("");

        btnSaveasDocument.setDisable(true);
        btnSaveasDocument.setOpacity(0.5);

        btnFetchFiles.setDisable(false);
        btnFetchFiles.setOpacity(1.0);

        btnSendToExport.setDisable(true);
        btnSendToExport.setOpacity(0.5);

        btnFinishBox.setDisable(false);
        btnFinishBox.setOpacity(1.0);
    }


}