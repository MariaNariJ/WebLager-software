package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import dk.easv.be.DocumentGroup;

public class FileManager {

    private final TIFFService tiffService = new TIFFService();
    private final BarcodeService barcodeService = new BarcodeService();

    private final PageDAO pageDAO = new PageDAO();

    // ================= SAVE ROTATION =================
    public void updatePageRotation(Page page) {
        pageDAO.updatePageRotation(page);
    }
    // ================= PROCESS FILES =================

    public void proccesFilesFromApi(Consumer<Page> scannedPage) {
        boolean pageBarcode = false;
        int counter = 1;

        while (!pageBarcode) {
            List<Page> pages = tiffService.processTiff(counter);

            if (pages == null || pages.isEmpty()) {
                System.err.println("No pages found from the api");
                break;
            }
            counter++;
            for (Page page : pages) {
                try {
                    File file = new File(page.getPagePath());
                    BufferedImage image = tiffService.convertToImage(file);

                    String barcode  = barcodeService.scanBarcode(image);
                    page.setBarcode(barcode);

                    if (scannedPage != null) {
                        scannedPage.accept(page);
                    }

                    if (barcode != null && !barcode.trim().isEmpty()) {
                        pageBarcode = true;

                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error processing file: " + page.getPagePath());
                }
            }
        }
    }

    public InputStream getFileStream(Page page) {
        try {
            return new FileInputStream(page.getPagePath());
        } catch (Exception e) {
            System.err.println("Error getting file stream for page: " + page.getPagePath());
            return null;
        }
    }

    /**
     * Groups scanned pages into documents.
     * A new document starts whenever a barcode page is detected.
     *
     * @param pages scanned pages
     * @return grouped documents
     */
    public List<DocumentGroup> groupPagesIntoDocuments(List<Page> pages) {

        List<DocumentGroup> documentGroups = new ArrayList<>();

        DocumentGroup currentDocument = null;

        int documentCounter = 1;

        for (Page page : pages) {

            // Barcode pages start a new document
            boolean isBarcodePage =
                    page.getBarcode() != null &&
                            !page.getBarcode().isEmpty();

            if (isBarcodePage) {

                currentDocument = new DocumentGroup(
                        "Document " + documentCounter,
                        page.getBarcode()
                );

                documentGroups.add(currentDocument);

                documentCounter++;
            }

            // Add page to active document
            if (currentDocument != null) {
                currentDocument.addPage(page);
            }
        }

        return documentGroups;
    }
}