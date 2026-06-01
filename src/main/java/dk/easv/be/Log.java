package dk.easv.be;

import java.sql.Timestamp;

public class Log {
    private int id;
    private Timestamp timestamp;
    private String type;
    private String event;
    private String username;
    private String details;
    private String status;

    public Log(int id, Timestamp timestamp, String type, String event,
               String username, String details, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.event = event;
        this.username = username;
        this.details = details;
        this.status = status;
    }

    public int getId() { return id; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getEvent() { return event; }
    public String getUsername() { return username; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
}