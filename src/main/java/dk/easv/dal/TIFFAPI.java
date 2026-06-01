package dk.easv.dal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Legacy API integration retained from an earlier scanning prototype.
 *
 * The final application uses Local Boxes and FileManager.scanNextDocument()
 * for document processing instead of the external TIFF API.
 */
public class TIFFAPI {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl = "https://studentiffapi-production.up.railway.app";


    public String getRandomFile() {
        try {
            String documentsPath = System.getProperty("user.home") + "/Documents";
            String tiffAppFolder = documentsPath + "/TIFFApp_tiffs";
            File tiffAppDir = new File(tiffAppFolder);
            if (!tiffAppDir.exists() && !tiffAppDir.mkdir()) {
                throw new java.io.IOException("Failed to create directory: " + tiffAppFolder);
            }

            String downloadPath = tiffAppFolder + "Tiff.zip";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/getRandomFile")).GET().build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(downloadPath)) {
                in.transferTo(out);
            }
            return downloadPath;
        } catch (Exception e) {
            System.err.println("Failed getting TIFFs from the api");
            return null;
        }
    }
}
