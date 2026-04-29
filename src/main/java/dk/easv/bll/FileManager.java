package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileManager {

    private PageDAO pageDAO = new PageDAO();

    private TIFFService tiffService = new TIFFService();
    private BarcodeService barcodeService = new BarcodeService();

    public void saveFile(Page page) throws Exception {
        pageDAO.createFile(page);
    }

//    public List<Page> processAndScanFiles() {
//        List<Page> pages = tiffService.processAllTiffs();
//        if (pages == null) {
//            return null;
//        }
//        for (Page page : pages) {
//            try {
//                File file = new File(page.getPagePath());
//                BufferedImage image = tiffService.convertToImage(file);
//                String barcode = barcodeService.scanBarcode(image);
//                page.setBarcode(barcode);
//            } catch (Exception e) {
//                System.err.println("Error processing file: " + page.getPagePath());
//            }
//        }
//        return pages;
//    }

    public List<Page> proccesFilesInOrder(Consumer<Page> scannedPage) {
        List<Page> documentPages = new ArrayList<>();
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
                    documentPages.add(page);

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
        return documentPages;
    }
}