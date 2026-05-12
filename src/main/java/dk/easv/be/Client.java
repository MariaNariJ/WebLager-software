package dk.easv.be;

import java.sql.Timestamp;

public class Client {

    private int clientId;
    private String name;
    private int profilesCount;
    private Timestamp lastUpdated;

    public Client(int clientId,
                  String name,
                  int profilesCount,
                  Timestamp lastUpdated) {

        this.clientId = clientId;
        this.name = name;
        this.profilesCount = profilesCount;
        this.lastUpdated = lastUpdated;
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
}