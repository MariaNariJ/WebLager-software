package dk.easv.dal;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TIFFAPI {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl = "https://studentiffapi-production.up.railway.app";

    public int getCount() {
        try {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/getCount")).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return Integer.parseInt(response.body().trim());
        } catch (Exception e) {
            System.err.println("Failed getting count of TIFFs from the api");
            e.printStackTrace();
            return -1;
        }
    }

    public String getAllFiles() {
        try {
            String downloadPath = System.getProperty("java.io.tmpdir") + "TIFFApp_tiffs.zip";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/getAllFiles")).GET().build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(downloadPath)) {
                in.transferTo(out);
            }
            return downloadPath;
        } catch (Exception e) {
            System.err.println("Failed getting TIFFs from the api");
            e.printStackTrace();
            return null;
        }
    }
}
