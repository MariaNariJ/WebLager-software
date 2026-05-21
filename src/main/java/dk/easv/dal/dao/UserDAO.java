package dk.easv.dal.dao;

import dk.easv.be.User;
import dk.easv.bll.PasswordHasher;
import dk.easv.dal.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
                String name = rs.getString("name");
                String password = rs.getString("password");
                String salt = rs.getString("salt");
                String status = rs.getString("status");

                return new User(id, role, name, login, password, salt, status);
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
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM Users";

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("role"),
                        rs.getString("name"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("salt"),
                        rs.getString("status")
                );

                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }
    public void createUser(String name, String login, String plainPassword, String role) {
        String sql = "INSERT INTO Users (role, login, password, salt, name, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(plainPassword, salt);

            stmt.setString(1, role);
            stmt.setString(2, login);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, salt);
            stmt.setString(5, name);
            stmt.setString(6, "Active");
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed creating user", e);
        }
    }
    public void updateUserStatus(int userId, String status) {
        String sql = "UPDATE Users SET status = ? WHERE id = ?";

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, userId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed updating user status", e);
        }
    }
    public void updateUser(User user) {
        String sql = "UPDATE Users SET name = ?, login = ?, role = ?, status = ? WHERE id = ?";

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getStatus());
            stmt.setInt(5, user.getId());

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed updating user", e);
        }
    }

    public void updateUserPassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ?, salt = ? WHERE id = ?";

        try {
            String newSalt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(newPassword, newSalt);

            try (Connection conn = conMan.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedPassword);
                stmt.setString(2, newSalt);
                stmt.setInt(3, userId);

                stmt.executeUpdate();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed updating password", e);
        }
    }
    public void deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE id = ?";

        try (Connection conn = conMan.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed deleting user", e);
        }
    }
}
