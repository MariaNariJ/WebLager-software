package dk.easv.dal.dao;

import dk.easv.be.Document;
import dk.easv.dal.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    ConnectionManager conMan =  new ConnectionManager();

    public int insertDocument(Document document) {
        int generatedId = -1;
        try (Connection con = conMan.getConnection()) {
            String sql = "INSERT INTO Documents (Box_id, Barcode, Date, DocumentType, DocumentName) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, document.getBoxId());
            ps.setString(2, document.getBarcode());
            ps.setDate(3, document.getDate());
            ps.setString(4, document.getDocumentType());
            ps.setString(5, document.getDocumentName());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println("Failed to insert document" + e.getMessage());
            throw new RuntimeException(e);
        }
        return generatedId;
    }
    public List<Document> getDocumentsForBox(int boxId) {

        List<Document> documents = new ArrayList<>();

        try (Connection con = conMan.getConnection()) {

            String sql =
                    "SELECT * FROM Documents WHERE Box_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, boxId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Document document = new Document(

                        rs.getInt("Box_id"),
                        rs.getString("Barcode"),
                        rs.getDate("Date"),
                        rs.getString("DocumentName"),
                        rs.getString("DocumentType")
                );

                document.setId(
                        rs.getInt("Document_id")
                );

                documents.add(document);
            }

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return documents;
    }
}
