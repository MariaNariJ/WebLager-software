package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.TIFFAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Legacy service retained from an earlier API-based scanning prototype.
 *
 * The final implementation uses Local Boxes together with
 * FileManager.scanNextDocument() for document scanning.
 *
 * This service has been kept as a reference to the original
 * TIFF API integration developed during the project.
 */
public class TIFFService {
    TIFFAPI api = new TIFFAPI();


    /**
     * Legacy method used for retrieving and extracting TIFF files
     * from the original API-based scanning solution.
     */
    public List<Page> processTiff(int counter) {
        List<Page> pages = new ArrayList<>();
        Path extractPath = Paths.get(System.getProperty("user.home"), "/Documents/TIFFApp_tiffs");

        String zipPath = api.getRandomFile();
        if (zipPath == null) {
            System.err.println("Failed getting TIFFs from the api");
            return null;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String newName = timestamp + "_" + counter + ".tiff";
                String newId = timestamp + counter;

                Path entryPath = extractPath.resolve(newName).normalize();

                File outFile = entryPath.toFile();
                File parentDir = outFile.getParentFile();

                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    zis.transferTo(out);
                }

                pages.add(new Page(newId, counter, -1, newName, outFile.getPath(), 0));
            }
        } catch (IOException | SecurityException e) {
            System.err.println("An error occurred while extracting the TIFF zip file: " + e);
        } finally {
            new File(zipPath).delete();
        }
        return pages;
    }

    public BufferedImage convertToImage(File file) throws IOException {
        return ImageIO.read(file);
    }
}
