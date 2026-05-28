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
                SELECT l.Log_id, l.Timestamp, l.Level, l.Type, l.Event,
                       u.login AS Username, l.Details, l.Status, l.Duration
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
                            rs.getString("Level"),
                            rs.getString("Type"),
                            rs.getString("Event"),
                            rs.getString("Username"),
                            rs.getString("Details"),
                            rs.getString("Status"),
                            rs.getString("Duration")
                    ));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not load logs from database", e);
        }

        return logs;
    }
    public void createLog(String level, String type, String event, Integer userId,
                          String details, String status, String duration) {
        String sql = """
            INSERT INTO Logs (Level, Type, Event, User_id, Details, Status, Duration)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, level);
            ps.setString(2, type);
            ps.setString(3, event);

            if (userId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, userId);
            }

            ps.setString(5, details);
            ps.setString(6, status);
            ps.setString(7, duration);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Could not create log", e);
        }
    }
}