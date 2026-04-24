package dk.easv.be;

public class File {
    private int id;
    private String filePath;
    private int order;
    private int documentId;

    public File(int id, String filePath, int order, int documentId) {
        this.id = id;
        this.filePath = filePath;
        this.order = order;
        this.documentId = documentId;
    }

    public int getId() { return id; }
    public String getFilePath() { return filePath; }
    public int getOrder() { return order; }
    public int getDocumentId() { return documentId; }
}

