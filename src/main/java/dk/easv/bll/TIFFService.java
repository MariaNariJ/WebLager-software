package dk.easv.bll;

import dk.easv.be.File;
import dk.easv.dal.TIFFAPI;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TIFFService {
    TIFFAPI api = new TIFFAPI();

    public int getCount() {
        try {
            return api.getCount();
        } catch (Exception e) {
            System.err.println("Failed getting count of TIFFs from the api");
            e.printStackTrace();
        }
        return 0;
    }

    public List<File> processTiffs() throws Exception {
        String zipPath = api.getAllFiles();
        String extractPath = System.getProperty("java.io.tmpdir") + "/TIFFApp_tiffs/";
        List<File> result = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                java.io.File outFile = new java.io.File(extractPath + entry.getName());
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    zis.transferTo(fos);
                }

                result.add(new File(outFile.getName(), outFile.getPath(), 0));
            }
        }
        return result;
    }

}
