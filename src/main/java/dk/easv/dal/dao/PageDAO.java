package dk.easv.dal.dao;

import dk.easv.be.Page;
import dk.easv.dal.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PageDAO {

    private final ConnectionManager cm = new ConnectionManager();

    // ================= CREATE =================
    public void createFile(Page page) throws SQLException {

        String sql = "INSERT INTO Files (fileName, filePath, rotation) VALUES (?, ?, ?)";

        try (Connection conn = cm.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, page.getPageName());   // FIXED
            stmt.setString(2, page.getPagePath());
            stmt.setInt(3, page.getRotation());

            stmt.executeUpdate();
        }
    }

    // ================= UPDATE ROTATION =================
    public void updatePageRotation(Page page) {

        String sql = "UPDATE Files SET rotation = ? WHERE id = ?"; // FIXED table name

        try (Connection conn = cm.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, page.getRotation());
            stmt.setInt(2, page.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}