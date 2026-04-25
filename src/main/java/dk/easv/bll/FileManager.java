package dk.easv.bll;

import dk.easv.be.Page;
import dk.easv.dal.dao.PageDAO;

public class FileManager {

    private PageDAO pageDAO = new PageDAO();

    public void saveFile(Page page) throws Exception {
        pageDAO.createFile(page);
    }
}