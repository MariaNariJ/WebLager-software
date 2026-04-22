package dk.easv.dal.dao;

import dk.easv.be.Document;
import dk.easv.dal.ConnectionManager;

import java.sql.*;

public class DocumentDAO {

    public void createDocument(Document doc) throws SQLException {

        String sql = "INSERT INTO Document (name, status, boxId) VALUES (?, ?, ?)";

        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doc.getName());
            stmt.setString(2, doc.getStatus());
            stmt.setInt(3, doc.getBoxId());

            stmt.executeUpdate();
        }
    }
}
