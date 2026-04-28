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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TIFFService {
    TIFFAPI api = new TIFFAPI();

    public int getCount() {
        return api.getCount();
    }
    
    public List<Page> processAllTiffs() {
        List<Page> pages = new ArrayList<>();
        Path extractPath = Paths.get(System.getProperty("user.home"), "/Documents/TIFFApp_tiffs");
        File existingDir = extractPath.toFile();

        if (existingDir.exists() && existingDir.isDirectory()) {
            File[] existingFiles = existingDir.listFiles();

            if (existingFiles != null && existingFiles.length > 0) {
                Arrays.sort(existingFiles, Comparator.comparing(File::lastModified));
                for (File file : existingFiles) {
                    pages.add(new Page(file.getName(), file.getPath(), 0));
                }
                return pages;
            }
        }

        String zipPath = api.getAllFiles();

        if (zipPath == null) {
            System.err.println("Failed getting TIFFs from the api");
            return null;
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                Path entryPath = extractPath.resolve(entry.getName()).normalize();
                /*
                * Safety precautions so that a malicious/broken zip file cannot overwrite
                * sensitive OS files.
                */
                if (!entryPath.startsWith(extractPath)) {
                    throw new SecurityException("Entry is outside of the target directory: " + entry.getName());
                }

                File outFile = entryPath.toFile();
                File parentDir = outFile.getParentFile();

                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    zis.transferTo(out);
                }

                pages.add(new Page(outFile.getName(), outFile.getPath(), 0)); //Needs proper ID generator
            }
        } catch (IOException | SecurityException e) {
            System.err.println("An error occurred while extracting the TIFFs zip file: " + e);
        } finally {
            new File(zipPath).delete();
        }
        return pages;
    }

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
                int newId = timestamp.hashCode() + counter;

                Path entryPath = extractPath.resolve(newName).normalize();

                File outFile = entryPath.toFile();
                File parentDir = outFile.getParentFile();

                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    zis.transferTo(out);
                }

                pages.add(new Page(newName, outFile.getPath(), newId));
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
