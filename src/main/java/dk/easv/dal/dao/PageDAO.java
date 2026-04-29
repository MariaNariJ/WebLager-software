package dk.easv.dal.dao;

import dk.easv.be.Page;
import dk.easv.dal.ConnectionManager;

import java.sql.*;

public class PageDAO {

    public void createFile(Page page) throws SQLException {

        String sql = "INSERT INTO Files (fileName, filePath) VALUES (?, ?)";

        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, page.getPagePath());
            stmt.setString(2, page.getPagePath());

            stmt.executeUpdate();
        }
    }
}