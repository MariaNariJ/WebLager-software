package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;
import dk.easv.bll.interfaces.IFileManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class FileManager implements IFileManager {

    private final TIFFService tiffService = new TIFFService();
    private final BarcodeService barcodeService = new BarcodeService();
    private final PageDAO pageDAO = new PageDAO();
    // Keeps track of where scanning stopped
    private int currentFileIndex = 0;
    // Cached TIFF files from local box
    private List<File> cachedFiles = new ArrayList<>();
    private String cachedBoxName = null;

    @Override
    //SAVE ROTATION
    public void updatePageRotation(Page page) {
        pageDAO.updatePageRotation(page);
    }

    // Main scanning workflow
    @Override
    public boolean scanNextDocument(
            String boxName,
            Consumer<Page> scannedPage) {

        boolean loaded = loadLocalBoxFiles(boxName);

        if (!loaded) {
            return false;
        }

        if (currentFileIndex >= cachedFiles.size()) {

            System.out.println("No more files");

            return false;
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
                        String.valueOf(System.currentTimeMillis() + currentFileIndex + pageNumber),
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
        return true;
    }

    @Override
    public void resetLocalBoxScan() {
        cachedFiles = new ArrayList<>();
        cachedBoxName = null;
        currentFileIndex = 0;
    }

    @Override
    public boolean hasMoreFiles() {
        return currentFileIndex < cachedFiles.size();
    }

    @Override
    public boolean localBoxExists(String boxName) {
        if (boxName == null || boxName.trim().isEmpty()) {
            return false;
        }

        File folder = new File("LocalBoxes/" + boxName.trim());

        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".tif") ||
                        name.toLowerCase().endsWith(".tiff")
        );

        return files != null && files.length > 0;
    }

    //Helper method used by ScanNextDocument(), loads and caches TIFF files from the selected box
    private boolean loadLocalBoxFiles(String boxName) {
        if (!cachedFiles.isEmpty() && boxName.equals(cachedBoxName)) {
            return true;
        }

        cachedFiles = new ArrayList<>();
        currentFileIndex = 0;
        cachedBoxName = boxName;

        File folder = new File("LocalBoxes/" + boxName);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Local box folder not found");
            return false;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".tif") ||
                        name.toLowerCase().endsWith(".tiff")
        );

        if (files == null || files.length == 0) {
            return false;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        cachedFiles = new ArrayList<>(Arrays.asList(files));
        return true;
    }

    /**
     * Legacy method retained from an earlier prototype.
     * The current scanning workflow uses scanNextDocument().
     */
    // This is the actual method from processing files form the API
    public void processFilesFromApi(Consumer<Page> scannedPage) {
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
    /**
     * Legacy method retained from an earlier prototype.
     * The current scanning workflow uses scanNextDocument().
     */
    public InputStream getFileStream(Page page) {
        try {
            return new FileInputStream(page.getPagePath());
        } catch (Exception e) {
            System.err.println("Error getting file stream for page: " + page.getPagePath());
            return null;
        }
    }
}