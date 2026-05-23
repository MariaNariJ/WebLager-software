package dk.easv.bll;

import dk.easv.be.Box;
import dk.easv.be.Document;
import dk.easv.be.DocumentGroup;
import dk.easv.be.Page;
import dk.easv.dal.dao.BoxDAO;
import dk.easv.dal.dao.DocumentDAO;
import dk.easv.dal.dao.PageDAO;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DocumentManager {

    private final BoxDAO boxDAO = new BoxDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PageDAO pageDAO = new PageDAO();

    public void saveDocumentToQA(
            DocumentGroup documentGroup,
            String client,
            String boxName,
            String documentName,
            String date,
            String selectedProfile
    ) {
        Box box = new Box(boxName, client);
        int boxId = boxDAO.insertBox(box);

        LocalDate parsedDate = LocalDate.parse(
                date,
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        Document document = new Document(
                boxId,
                documentGroup.getBarcode(),
                java.sql.Date.valueOf(parsedDate),
                documentName,
                selectedProfile
        );

        int documentId = documentDAO.insertDocument(document);

        for (Page page : documentGroup.getPages()) {
            page.setDocumentId(documentId);

            try (InputStream inputStream = new FileInputStream(page.getPagePath())) {
                pageDAO.insertPage(page, inputStream);
            } catch (Exception e) {
                throw new RuntimeException("Failed saving page: " + page.getPageName(), e);
            }
        }
    }
}