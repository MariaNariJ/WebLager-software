package dk.easv.be;

public class File {
    private String fileName;
    private String filePath;
    private int documentId;

    public File(String fileName, String filePath, int documentId) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getDocumentId() {
        return documentId;
    }
}