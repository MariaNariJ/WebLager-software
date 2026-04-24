package dk.easv.gui;

import dk.easv.bll.TIFFService;
import dk.easv.be.File;

import java.util.List;

public class userviewController {

    private TIFFService apiService = new TIFFService();

    public void onFetchFilesClicked() {
//        List<File> files = apiService.fetchFiles(10);
//
//        for (File file : files)
//            System.out.println(file);
//        }
        System.out.println(apiService.getCount());
    }
}