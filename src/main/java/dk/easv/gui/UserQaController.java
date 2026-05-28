package dk.easv.gui;

import dk.easv.be.Document;
import dk.easv.be.DocumentGroup;
import dk.easv.bll.DocumentManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import dk.easv.be.Box;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.scene.control.ScrollPane;
import dk.easv.be.Page;
import java.util.ArrayList;

public class UserQaController {

    @FXML
    private VBox queueContainer;
    @FXML
    private Label clientLabel;
    @FXML
    private Label boxLabel;
    @FXML
    private Label documentNameLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label barcodeLabel;
    @FXML
    private Label scanProfileLabel;
    @FXML
    private Label qaStatusLabel;
    @FXML
    private Button approveButton;
    @FXML
    private Label queueCountLabel;
    @FXML
    private ImageView previewImage;
    @FXML
    private ScrollPane previewScrollPane;

    private final DocumentManager documentManager =
            new DocumentManager();
    private List<Page> currentPages =
            new ArrayList<>();

    private int currentPageIndex = 0;

    private Image currentImage;




    public void initialize() {
        loadQaDocuments();
        previewImage.setPreserveRatio(true);

        previewImage.setFitWidth(700);

        previewScrollPane.setPannable(true);

    }
    @FXML
    private void onApproveDocument() {

        if (!queueContainer.getChildren().isEmpty()) {
            queueContainer.getChildren().remove(0);
        }

        qaStatusLabel.setText("Approved");
    }

    private void loadQaDocuments() {

        List<Box> boxes =
                documentManager.getBoxesForQA();

        queueContainer.getChildren().clear();

        for (Box box : boxes) {

            VBox card = createBoxCard(box);

            queueContainer.getChildren().add(card);
        }

        queueCountLabel.setText(
                String.valueOf(boxes.size())
        );
    }

    private VBox createBoxCard(Box box) {

        VBox card = new VBox(7);

        card.getStyleClass().add("qa-document-card");

        Label title =
                new Label(box.getBoxName());

        title.getStyleClass().add("qa-document-title");

        Label client =
                new Label(box.getClientName());

        client.getStyleClass().add("qa-small-text");

        card.getChildren().addAll(
                title,
                client
        );

        card.setOnMouseClicked(e -> {

            loadDocumentsForBox(box);
        });

        return card;
    }

    private void selectDocument(Document document){

        barcodeLabel.setText(document.getBarcode());

        documentNameLabel.setText(
                document.getDocumentName()
        );
        qaStatusLabel.setText("Waiting for QA");
    }

    private void loadDocumentsForBox(Box box) {

        List<Document> documents =
                documentManager.getDocumentsForBox(
                        box.getId()
                );

        if (documents.isEmpty()) {
            return;
        }

        Document firstDocument =
                documents.get(0);
        currentPages =
                documentManager.getPagesForDocument(
                        firstDocument.getId()
                );

        currentPageIndex = 0;

        clientLabel.setText(
                box.getClientName()
        );

        boxLabel.setText(
                box.getBoxName()
        );

        documentNameLabel.setText(
                firstDocument.getDocumentName()
        );

        barcodeLabel.setText(
                firstDocument.getBarcode()
        );

        scanProfileLabel.setText(
                firstDocument.getDocumentType()
        );

        dateLabel.setText(
                firstDocument.getDate().toString()
        );

        qaStatusLabel.setText(
                "Waiting for QA"
        );

        currentPages =
                documentManager.getPagesForDocument(
                        firstDocument.getId()
                );

        if (!currentPages.isEmpty()) {

            showPage(
                    currentPages.get(currentPageIndex)
            );
        }
    }
    private void showPage(Page page) {

        try {

            BufferedImage bufferedImage =
                    ImageIO.read(
                            new ByteArrayInputStream(
                                    page.getImageData()
                            )
                    );

            if (bufferedImage == null) {
                return;
            }

            currentImage =
                    SwingFXUtils.toFXImage(
                            bufferedImage,
                            null
                    );

            previewImage.setImage(currentImage);
            previewImage.setFitWidth(700);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    @FXML
    private void onNextPage() {

        if (currentPages == null) {
            return;
        }

        if (currentPageIndex <
                currentPages.size() - 1) {

            currentPageIndex++;

            showPage(
                    currentPages.get(currentPageIndex)
            );
        }
    }

    @FXML
    private void onPreviousPage() {

        if (currentPages == null) {
            return;
        }

        if (currentPageIndex > 0) {

            currentPageIndex--;

            showPage(
                    currentPages.get(currentPageIndex)
            );
        }
    }
}

