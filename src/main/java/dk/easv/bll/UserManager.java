package dk.easv.bll;

import dk.easv.be.User;
import dk.easv.dal.dao.UserDAO;

import java.util.List;

public class UserManager {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public void createUser(String name, String login, String password, String role) {
        try {
            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(password, salt);

            userDAO.createUser(name, login, hashedPassword, salt, role);

        } catch (Exception e) {
            throw new RuntimeException("Failed creating user", e);
        }
    }

    public void updateUserStatus(int userId, String status) {
        userDAO.updateUserStatus(userId, status);
    }

    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    public void updateUserPassword(int userId, String newPassword) {
        try {
            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(newPassword, salt);

            userDAO.updateUserPassword(userId, hashedPassword, salt);

        } catch (Exception e) {
            throw new RuntimeException("Failed updating user password", e);
        }
    }
    public void deleteUser(int userId) {
        userDAO.deleteUser(userId);
    }
}