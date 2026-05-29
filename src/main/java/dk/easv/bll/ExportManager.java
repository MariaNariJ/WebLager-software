package dk.easv.bll;

import dk.easv.be.Document;
import dk.easv.be.Page;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;

public class ExportManager {

    private final DocumentManager documentManager = new DocumentManager();

    public int exportSinglePageTiffs(List<Document> documents, File destinationFolder) {
        int exportedCount = 0;

        for (Document document : documents) {
            List<Page> pages = documentManager.getPagesForDocument(document.getId());

            for (Page page : pages) {
                exportSinglePage(document, page, destinationFolder);
                exportedCount++;
            }
        }

        return exportedCount;
    }

    public int exportMultiPageTiffs(List<Document> documents, File destinationFolder) {
        int exportedCount = 0;

        for (Document document : documents) {
            List<Page> pages = documentManager.getPagesForDocument(document.getId());

            if (pages == null || pages.isEmpty()) {
                continue;
            }

            exportMultiPageDocument(document, pages, destinationFolder);
            exportedCount++;
        }

        return exportedCount;
    }

    private void exportSinglePage(Document document, Page page, File destinationFolder) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(page.getImageData()));

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

    private void exportMultiPageDocument(Document document, List<Page> pages, File destinationFolder) {
        String fileName = cleanFileName(document.getDocumentName()) + ".tiff";
        File outputFile = new File(destinationFolder, fileName);

        try {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");

            if (!writers.hasNext()) {
                throw new RuntimeException("No TIFF writer found.");
            }

            ImageWriter writer = writers.next();

            try (ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(output);

                ImageWriteParam params = writer.getDefaultWriteParam();

                writer.prepareWriteSequence(null);

                for (Page page : pages) {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(page.getImageData()));

                    if (image == null) {
                        throw new RuntimeException("Could not read page image: " + page.getPageName());
                    }

                    writer.writeToSequence(new IIOImage(image, null, null), params);
                }

                writer.endWriteSequence();
            } finally {
                writer.dispose();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed exporting multi-page TIFF: " + document.getDocumentName(), e);
        }
    }

    private String cleanFileName(String value) {
        if (value == null || value.isBlank()) {
            return "document";
        }

        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}