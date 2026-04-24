package dk.easv.dal.dao;

import dk.easv.be.File;
import dk.easv.dal.ConnectionManager;

import java.sql.*;

public class FileDAO {

    public void createFile(File file) throws SQLException {

        String sql = "INSERT INTO Files (fileName, filePath, documentId) VALUES (?, ?, ?)";

        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, file.getFileName());
            stmt.setString(2, file.getFilePath());
            stmt.setInt(3, file.getDocumentId());

            stmt.executeUpdate();
        }
    }
}