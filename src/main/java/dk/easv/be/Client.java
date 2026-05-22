package dk.easv.be;

import java.sql.Timestamp;

public class Client {

    private int clientId;
    private String name;
    private int profilesCount;
    private Timestamp lastUpdated;
    private String status;

    public Client(int clientId,
                  String name,
                  int profilesCount,
                  Timestamp lastUpdated,
                  String status) {

        this.clientId = clientId;
        this.name = name;
        this.profilesCount = profilesCount;
        this.lastUpdated = lastUpdated;
        this.status = status;
    }

    public int getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public int getProfilesCount() {
        return profilesCount;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public String getStatus() {
         return status;}

    public void setStatus(String status) {
        this.status = status;
    }
}