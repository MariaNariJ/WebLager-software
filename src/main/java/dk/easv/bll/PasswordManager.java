package dk.easv.bll;

import dk.easv.be.User;
import dk.easv.dal.dao.UserDAO;

public class PasswordManager {
    UserDAO dao = new UserDAO();
    User user;

    public boolean checkPassword(String login, String password) {
        try {
            user = dao.getUser(login);
            return PasswordHasher.verifyPassword(password, user.getPassword(), user.getSalt());
        } catch (Exception e) {
            return false;
        }
    }

    public User getUser() {
        return user;
    }
}
