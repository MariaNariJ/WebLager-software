package dk.easv.bll;

import dk.easv.be.Document;
import dk.easv.be.Page;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class ExportManager {

    private final DocumentManager documentManager = new DocumentManager();

    public void exportSinglePageTiffs(int boxId, File destinationFolder) {
        List<Document> documents = documentManager.getDocumentsForBox(boxId);

        for (Document document : documents) {
            List<Page> pages = documentManager.getPagesForDocument(document.getId());

            for (Page page : pages) {
                exportSinglePage(document, page, destinationFolder);
            }
        }
    }

    private void exportSinglePage(Document document, Page page, File destinationFolder) {
        try {
            var image = ImageIO.read(new ByteArrayInputStream(page.getImageData()));

            if (image == null) {
                throw new RuntimeException("Could not read image: " + page.getPageName());
            }

            String fileName =
                    cleanFileName(document.getDocumentName()) +
                            "_page_" +
                            page.getOrderId() +
                            ".tiff";

            File outputFile = new File(destinationFolder, fileName);

            ImageIO.write(image, "TIFF", outputFile);

        } catch (Exception e) {
            throw new RuntimeException("Failed exporting page: " + page.getPageName(), e);
        }
    }

    private String cleanFileName(String value) {
        if (value == null || value.isBlank()) {
            return "document";
        }

        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}