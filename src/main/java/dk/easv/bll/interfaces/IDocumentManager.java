package dk.easv.bll.interfaces;

import dk.easv.be.Box;
import dk.easv.be.Document;
import dk.easv.be.DocumentGroup;
import dk.easv.be.Page;

import java.util.List;

public interface IDocumentManager {

    void saveDocumentForExport(
            DocumentGroup documentGroup,
            int boxId,
            String documentName,
            String date,
            String selectedProfile
    );

    void saveBoxForExport(
            List<DocumentGroup> documentGroups,
            String client,
            String boxName,
            String profile,
            String date
    );

    List<Box> getBoxesForExport();

    List<Document> getDocumentsForBox(int boxId);

    List<Page> getPagesForDocument(int documentId);
}