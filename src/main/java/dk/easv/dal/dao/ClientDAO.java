package dk.easv.dal.dao;

import dk.easv.be.Client;
import dk.easv.dal.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import dk.easv.be.Profile;

public class ClientDAO {

    private final ConnectionManager conMan = new ConnectionManager();

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();

        String sql = """
        SELECT client_id,
        name,
        profiles_count,
        last_updated,
        status
        FROM Clients
        ORDER BY name
        """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getInt("profiles_count"),
                        rs.getTimestamp("last_updated"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            System.out.println("Failed getting clients: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return clients;
    }
    public void createClient(String name) {
        String sql = """
            INSERT INTO Clients (name, profiles_count, last_updated, status)
            OUTPUT INSERTED.client_id
            VALUES (?, 0, GETDATE(), ?)
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, "Active");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int clientId = rs.getInt(1);

                createProfile(clientId, "Standard Scan", "Default scan profile for normal documents.");
                createProfile(clientId, "Bright Document", "Profile for darker scans that need higher brightness.");
                createProfile(clientId, "Dark Document", "Profile for very bright documents that need lower brightness.");
                createProfile(clientId, "Rotated Scan", "Profile for documents that need a different base rotation.");
            }

        } catch (Exception e) {
            System.out.println("Failed creating client: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void updateClientStatus(int clientId, String status) {
        String sql = "UPDATE Clients SET status = ?, last_updated = GETDATE() WHERE client_id = ?";

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, clientId);

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed updating client status: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Profile> getProfilesByClientId(int clientId) {
        List<Profile> profiles = new ArrayList<>();

        String sql = """
            SELECT ProfileID, ClientID, Name, Description
            FROM Profiles
            WHERE ClientID = ?
            ORDER BY Name
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clientId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                profiles.add(new Profile(
                        rs.getInt("ProfileID"),
                        rs.getInt("ClientID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                ));
            }

        } catch (Exception e) {
            System.out.println("Failed getting profiles: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return profiles;
    }

    public void createProfile(int clientId, String name, String description) {
        String sql = """
            INSERT INTO Profiles (ClientID, Name, Description)
            VALUES (?, ?, ?)
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            ps.setString(2, name);
            ps.setString(3, description);

            ps.executeUpdate();

            updateClientProfileCount(clientId);

        } catch (Exception e) {
            System.out.println("Failed creating profile: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void updateClientProfileCount(int clientId) {
        String sql = """
            UPDATE Clients
            SET profiles_count = (
                SELECT COUNT(*)
                FROM Profiles
                WHERE ClientID = ?
            ),
            last_updated = GETDATE()
            WHERE client_id = ?
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            ps.setInt(2, clientId);

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Failed updating profile count: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void deleteClient(int clientId) {
        String deleteProfilesSql = "DELETE FROM Profiles WHERE ClientID = ?";
        String deleteClientSql = "DELETE FROM Clients WHERE client_id = ?";

        try (Connection con = conMan.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement psProfiles = con.prepareStatement(deleteProfilesSql);
                 PreparedStatement psClient = con.prepareStatement(deleteClientSql)) {

                psProfiles.setInt(1, clientId);
                psProfiles.executeUpdate();

                psClient.setInt(1, clientId);
                psClient.executeUpdate();

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {
            System.out.println("Failed deleting client: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void updateProfile(int profileId, int clientId, String name, String description) {
        String sql = """
            UPDATE Profiles
            SET Name = ?, Description = ?
            WHERE ProfileID = ?
            """;

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, profileId);

            ps.executeUpdate();

            updateClientProfileCount(clientId);

        } catch (Exception e) {
            System.out.println("Failed updating profile: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteProfile(int profileId, int clientId) {
        String sql = "DELETE FROM Profiles WHERE ProfileID = ?";

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, profileId);
            ps.executeUpdate();

            updateClientProfileCount(clientId);

        } catch (Exception e) {
            System.out.println("Failed deleting profile: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}