package dk.easv.bll;

import dk.easv.be.Page;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class FileManager {

    private final TIFFService tiffService = new TIFFService();
    private final BarcodeService barcodeService = new BarcodeService();

    private final PageDAO pageDAO = new PageDAO();

    // ================= SAVE ROTATION =================
    public void updatePageRotation(Page page) {
        pageDAO.updatePageRotation(page);
    }

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
}