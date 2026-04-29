package dk.easv.dal.dao;

import dk.easv.be.Page;
import dk.easv.dal.ConnectionManager;

import java.io.InputStream;
import java.sql.*;

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
}