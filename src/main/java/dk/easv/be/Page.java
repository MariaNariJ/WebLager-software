package dk.easv.be;

public class Page {
    private String pageName;
    private String pagePath;
    private int documentId;

    public Page (String fileName, String filePath, int documentId) {
        this.pageName = fileName;
        this.pagePath = filePath;
        this.documentId = documentId;
    }

    public String getPageName() {
        return pageName;
    }

    public String getPagePath() {
        return pagePath;
    }

    public int getDocumentId() {
        return documentId;
    }
}