package dk.easv.bll;

import dk.easv.be.Client;
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
}