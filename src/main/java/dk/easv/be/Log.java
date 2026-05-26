package dk.easv.be;

import java.sql.Timestamp;

public class Log {
    private int id;
    private Timestamp timestamp;
    private String level;
    private String type;
    private String event;
    private String username;
    private String details;
    private String status;
    private String duration;

    public Log(int id, Timestamp timestamp, String level, String type, String event,
               String username, String details, String status, String duration) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.type = type;
        this.event = event;
        this.username = username;
        this.details = details;
        this.status = status;
        this.duration = duration;
    }

    public int getId() { return id; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getType() { return type; }
    public String getEvent() { return event; }
    public String getUsername() { return username; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public String getDuration() { return duration; }
}