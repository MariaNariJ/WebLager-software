package dk.easv.gui;

import dk.easv.bll.TiffApiService;
import dk.easv.be.File;

import java.util.List;

public class userviewController {

    private TiffApiService apiService = new TiffApiService();

    public void onFetchFilesClicked() {
        List<File> files = apiService.fetchFiles(10);

        for (File file : files) {
            System.out.println(file.getFilePath());
        }
    }
}