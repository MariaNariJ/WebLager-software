package dk.easv.dal.dao;

import dk.easv.dal.ConnectionManager;
import dk.easv.be.Box;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class BoxDAO {
    private final ConnectionManager conMan = new ConnectionManager();

    public int insertBox(Box box) {
        int generatedId = -1;
        try (Connection con = conMan.getConnection()) {
            String sql = "INSERT INTO Boxes (BoxName, ClientName) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, box.getBoxName());
            ps.setString(2, box.getClientName());

            generatedId = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println("Failed to insert box" + e.getMessage());
            throw new RuntimeException(e);
        }
        return generatedId;
    }

    public List<Box> getAllBoxes() {
        List<Box> boxes = new ArrayList<>();
        try (Connection con = conMan.getConnection()) {

            String sql = "SELECT * FROM Boxes ORDER BY Box_id DESC";
            PreparedStatement ps =
                    con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Box box = new Box(

                        rs.getString("BoxName"),
                        rs.getString("ClientName")
                );

                box.setId(rs.getInt("Box_id"));
                boxes.add(box);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return boxes;
    }
}
