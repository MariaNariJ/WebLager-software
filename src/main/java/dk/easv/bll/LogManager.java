package dk.easv.bll;

import dk.easv.be.Log;
import dk.easv.dal.dao.LogDAO;

import java.util.List;

public class LogManager {

    private final LogDAO logDAO = new LogDAO();

    public List<Log> getLogsByType(String type) {
        return logDAO.getLogsByType(type);
    }
    public void createLog(String level, String type, String event, Integer userId,
                          String details, String status, String duration) {
        logDAO.createLog(level, type, event, userId, details, status, duration);
    }
}