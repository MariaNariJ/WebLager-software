package dk.easv.be;

public class File {

    private int id;
    private String fileName;
    private String filePath;
    private int documentId;

    public File() {}

    public File(int id, String fileName, String filePath, int documentId) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.documentId = documentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }
}