package dk.easv.bll;

import dk.easv.be.Client;
import dk.easv.be.Profile;
import dk.easv.dal.dao.ClientDAO;

import java.util.List;

public class ClientManager {

    private final ClientDAO clientDAO = new ClientDAO();

    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    public void createClient(String clientName) {
        clientDAO.createClient(clientName);
    }

    public void updateClientStatus(int clientId, String status) {
        clientDAO.updateClientStatus(clientId, status);
    }

    public void deleteClient(int clientId) {
        clientDAO.deleteClient(clientId);
    }

    public List<Profile> getProfilesByClientId(int clientId) {
        return clientDAO.getProfilesByClientId(clientId);
    }

    public void createProfile(int clientId, String name, String description) {
        clientDAO.createProfile(clientId, name, description);
    }

    public void updateProfile(int profileId, int clientId, String name, String description) {
        clientDAO.updateProfile(profileId, clientId, name, description);
    }

    public void deleteProfile(int profileId, int clientId) {
        clientDAO.deleteProfile(profileId, clientId);
    }
}