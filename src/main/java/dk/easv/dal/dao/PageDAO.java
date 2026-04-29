package dk.easv.dal.dao;

import dk.easv.be.Page;
import dk.easv.dal.ConnectionManager;

import java.io.InputStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PageDAO {
    ConnectionManager conMan = new ConnectionManager();

    public void insertPage(Page page, InputStream inputStream) {
        try (Connection con = conMan.getConnection()) {
            String sql = "INSERT INTO Files (File_id, Order_id, Document_id, Image, FileName, Rotation) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, page.getPageId());
            ps.setInt(2, page.getOrderId());
            ps.setInt(3, page.getDocumentId());
            ps.setBinaryStream(4, inputStream);
            ps.setString(5, page.getPageName());
            ps.setInt(6, page.getRotation());

            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Failed to insert page: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ================= UPDATE ROTATION =================
    public void updatePageRotation(Page page) {

        String sql = "UPDATE Files SET rotation = ? WHERE File_id = ?"; // FIXED table name

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, page.getRotation());
            stmt.setString(2, page.getPageId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}