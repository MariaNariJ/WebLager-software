package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class FileManager {

    private final TIFFService tiffService = new TIFFService();
    private final BarcodeService barcodeService = new BarcodeService();

    private final PageDAO pageDAO = new PageDAO();

    // Keeps track of where scanning stopped
    private int currentFileIndex = 0;

    // Cached TIFF files from local box
    private List<File> cachedFiles = new ArrayList<>();

    //SAVE ROTATION
    public void updatePageRotation(Page page) {
        pageDAO.updatePageRotation(page);
    }


    // This is the actual method from processing files form the API, which will be commented out later on
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

    // This method only acts as a support for showing how it will work in real life
    public void processFilesFromLocalBox(Consumer<Page> scannedPage) {
        File folder = new File("LocalBoxes/Box_001");

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Local box folder not found: " + folder.getAbsolutePath());
            return;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".tif") ||
                        name.toLowerCase().endsWith(".tiff")
        );

        if (files == null || files.length == 0) {
            System.err.println("No TIFF files found in: " + folder.getAbsolutePath());
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        int pageNumber = 1;

        for (File file : files) {
            try {
                BufferedImage image = ImageIO.read(file);

                String barcode = null;

                try {
                    barcode = barcodeService.scanBarcode(image);
                } catch (Exception ignored) {
                }

                Page page = new Page(
                        String.valueOf(System.currentTimeMillis() + pageNumber),
                        pageNumber,
                        -1,
                        file.getName(),
                        file.getAbsolutePath(),
                        0
                );

                page.setBarcode(barcode);

                //This is not necessary since we are always passing it from the controller but for better practice!
                if (scannedPage != null) {
                    scannedPage.accept(page);
                }

                //Simulating the files fetching a bit slower so it feels closer to an actual API
                Thread.sleep(200 + new Random().nextInt(250));

                pageNumber++;

            } catch (Exception e) {
                System.err.println("Failed loading local TIFF: " + file.getName());
                e.printStackTrace();
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

    private void loadLocalBoxFiles() {

        if (!cachedFiles.isEmpty()) {
            return;
        }

        File folder = new File("LocalBoxes/Box_001");

        if (!folder.exists() || !folder.isDirectory()) {

            System.err.println(
                    "Local box folder not found"
            );

            return;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".tif") ||
                        name.toLowerCase().endsWith(".tiff")
        );

        if (files == null) {
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        cachedFiles = Arrays.asList(files);
    }
    public void scanNextDocument(
            Consumer<Page> scannedPage) {

        loadLocalBoxFiles();

        if (currentFileIndex >= cachedFiles.size()) {

            System.out.println("No more files");

            return;
        }

        boolean firstBarcodeFound = false;

        int pageNumber = 1;

        while (currentFileIndex < cachedFiles.size()) {

            File file = cachedFiles.get(currentFileIndex);

            try {

                BufferedImage image =
                        ImageIO.read(file);

                String barcode = null;

                try {
                    barcode =
                            barcodeService.scanBarcode(image);
                } catch (Exception ignored) {
                }

                Page page = new Page(
                        String.valueOf(
                                System.currentTimeMillis()
                        ),
                        pageNumber,
                        -1,
                        file.getName(),
                        file.getAbsolutePath(),
                        0
                );

                page.setBarcode(barcode);

                // Detect document boundaries
                boolean hasBarcode =
                        barcode != null &&
                                !barcode.isBlank();

                // First barcode starts document
                if (hasBarcode && !firstBarcodeFound) {

                    firstBarcodeFound = true;
                }

                // Second barcode stops scanning
                else if (hasBarcode) {

                    break;
                }

                if (scannedPage != null) {
                    scannedPage.accept(page);
                }

                currentFileIndex++;

                pageNumber++;

                Thread.sleep(200);

            } catch (Exception e) {

                e.printStackTrace();

                currentFileIndex++;
            }
        }
    }
}