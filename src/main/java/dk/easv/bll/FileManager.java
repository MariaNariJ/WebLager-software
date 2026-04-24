package dk.easv.bll;

import dk.easv.be.File;
import dk.easv.dal.dao.FileDAO;

public class FileManager {

    private FileDAO fileDAO = new FileDAO();

    public void saveFile(File file) throws Exception {
        fileDAO.createFile(file);
    }
}