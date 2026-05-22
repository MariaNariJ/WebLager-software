package dk.easv.be;

public class Profile {

    private int profileId;
    private int clientId;
    private String name;
    private String description;

    public Profile(int profileId, int clientId, String name, String description) {
        this.profileId = profileId;
        this.clientId = clientId;
        this.name = name;
        this.description = description;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}