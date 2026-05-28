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
import java.util.List;

public class DocumentManager {

    private final BoxDAO boxDAO = new BoxDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PageDAO pageDAO = new PageDAO();


    public void saveDocumentForExport(
            DocumentGroup documentGroup,
            int boxId,
            String documentName,
            String date,
            String selectedProfile
    ) {

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

            try (InputStream inputStream =
                         new FileInputStream(page.getPagePath())) {

                pageDAO.insertPage(page, inputStream);

            } catch (Exception e) {

                throw new RuntimeException(
                        "Failed saving page: "
                                + page.getPageName(),
                        e
                );
            }
        }
    }

    public void saveBoxForExport(
            List<DocumentGroup> documentGroups,
            String client,
            String boxName,
            String profile,
            String date
    ) {

        Box box = new Box(
                boxName,
                client
        );

        int boxId = boxDAO.insertBox(box);

        for (DocumentGroup documentGroup : documentGroups) {

            saveDocumentForExport(
                    documentGroup,
                    boxId,
                    documentGroup.getTitle(),
                    date,
                    profile
            );
        }
    }
    public List<Box> getBoxesForExport() {

        return boxDAO.getAllBoxes();
    }
    public List<Document> getDocumentsForBox(int boxId) {

        return documentDAO.getDocumentsForBox(boxId);
    }
    public List<Page> getPagesForDocument(
            int documentId
    ) {

        return pageDAO.getPagesForDocument(
                documentId
        );
    }
}