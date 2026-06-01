package dk.easv.dal.dao;

import dk.easv.be.Log;
import dk.easv.dal.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    private final ConnectionManager conMan = new ConnectionManager();

    public List<Log> getLogsByType(String type) {
        List<Log> logs = new ArrayList<>();

        String sql = """
                SELECT l.Log_id, l.Timestamp, l.Type, l.Event,
                       u.login AS Username, l.Details, l.Status
                FROM Logs l
                LEFT JOIN Users u ON l.User_id = u.id
                WHERE l.Type = ?
                ORDER BY l.Timestamp DESC
                """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new Log(
                            rs.getInt("Log_id"),
                            rs.getTimestamp("Timestamp"),
                            rs.getString("Type"),
                            rs.getString("Event"),
                            rs.getString("Username"),
                            rs.getString("Details"),
                            rs.getString("Status")
                    ));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not load logs from database", e);
        }

        return logs;
    }
    public void createLog(String type, String event, Integer userId,
                          String details, String status) {
        String sql = """
            INSERT INTO Logs (Type, Event, User_id, Details, Status)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setString(2, event);

            if (userId == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, userId);
            }

            ps.setString(4, details);
            ps.setString(5, status);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Could not create log", e);
        }
    }
}