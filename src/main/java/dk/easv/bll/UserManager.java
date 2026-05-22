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
        userDAO.createUser(name, login, password, role);
    }

    public void updateUserStatus(int userId, String status) {
        userDAO.updateUserStatus(userId, status);
    }

    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    public void updateUserPassword(int userId, String newPassword) {
        userDAO.updateUserPassword(userId, newPassword);
    }

    public void deleteUser(int userId) {
        userDAO.deleteUser(userId);
    }
}