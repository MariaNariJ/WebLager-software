package dk.easv.dal.dao;

import dk.easv.be.User;
import dk.easv.dal.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    ConnectionManager conMan = new ConnectionManager();

    public User getUser(String login) {
        try (Connection con = conMan.getConnection())
        {
            String sql = "SELECT * FROM Users WHERE login = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String password = rs.getString("password");
                String salt = rs.getString("salt");
                return new User(id, role, login, password, salt);
            } else {
                //No user was found
                return null;
            }
        } catch (Exception e) {
            //Need better error handling
            System.out.println("Failed getting users" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
