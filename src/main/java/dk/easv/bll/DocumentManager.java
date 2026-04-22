package dk.easv.bll;

import dk.easv.be.Document;
import dk.easv.dal.dao.DocumentDAO;

public class DocumentManager {

    public Document createDocument(String name, int boxId) {
        Document doc = new Document();
        doc.setName(name);
        doc.setStatus("In Progress");
        doc.setBoxId(boxId);

        try {
            documentDAO.createDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }
    private DocumentDAO documentDAO = new DocumentDAO();
}
