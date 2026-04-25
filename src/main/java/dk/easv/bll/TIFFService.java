package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.TIFFAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TIFFService {
    TIFFAPI api = new TIFFAPI();

    public int getCount() {
        return api.getCount();
    }

    public List<Page> processTiffs() {
        String zipPath = api.getAllFiles();
        List<Page> pages = new ArrayList<>();

        if (zipPath == null) {
            System.err.println("Failed getting TIFFs from the api");
            return null;
        }
        Path extractPath = Paths.get(System.getProperty("user.home"), "/Documents/TIFFApp_tiffs");

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
}
