package dk.easv.bll;

import dk.easv.be.File;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class TiffApiService {

    private static final String BASE_URL =
            "https://studentiffapi-production.up.railway.app";

    public List<File> fetchFiles(int amount) {
        List<File> files = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/getFiles/" + amount))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray jsonArray = new JSONArray(response.body());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                File file = new File(
                        obj.getInt("id"),
                        obj.getString("filePath"),
                        obj.getInt("order"),
                        obj.getInt("documentId")
                );

                files.add(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }
}