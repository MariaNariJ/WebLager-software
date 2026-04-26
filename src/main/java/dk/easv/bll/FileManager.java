package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import dk.easv.bll.FileManager;
import dk.easv.be.Page;

import java.util.List;

public class FileManager {

    private PageDAO pageDAO = new PageDAO();

    private TIFFService tiffService = new TIFFService();
    private BarcodeService barcodeService = new BarcodeService();

    public void saveFile(Page page) throws Exception {
        pageDAO.createFile(page);
    }

    public List<Page> processAndScanFiles() {

        List<Page> pages = tiffService.processTiffs();

        if (pages == null) {
            return null;
        }

        for (Page page : pages) {
            try {
                File file = new File(page.getPagePath());

                BufferedImage image = tiffService.convertToImage(file);

                String barcode = barcodeService.scanBarcode(image);

                page.setBarcode(barcode);

            } catch (Exception e) {
                System.err.println("Error processing file: " + page.getPagePath());
            }
        }

        return pages;
    }
}