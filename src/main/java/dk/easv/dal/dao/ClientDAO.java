package dk.easv.dal.dao;

import dk.easv.be.Client;
import dk.easv.dal.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "INSERT INTO Clients (name, profiles_count, last_updated, status) VALUES (?, 0, GETDATE(), ?)";

        try (Connection con = conMan.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, "Active");

            ps.executeUpdate();

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
}