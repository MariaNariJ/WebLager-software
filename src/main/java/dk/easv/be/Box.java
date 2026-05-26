package dk.easv.be;

public class Box {
    private String BoxName;
    private String ClientName;

    public Box(String BoxName, String ClientName) {
        this.BoxName = BoxName;
        this.ClientName = ClientName;
    }

    public String getBoxName() {
        return BoxName;
    }
    public void setBoxName(String BoxName) {
        this.BoxName = BoxName;
    }
    private int id;

    public String getClientName() {
        return ClientName;
    }
    public void setClientName(String ClientName) {
        this.ClientName = ClientName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
