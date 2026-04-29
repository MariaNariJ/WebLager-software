package dk.easv.be;

public class Document {
    private int id;
    private String name;
    private String status;
    private int boxId;


    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getBoxId() {
        return boxId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }
}
