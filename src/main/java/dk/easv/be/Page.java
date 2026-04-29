package dk.easv.be;

public class Page {
    private String pageId;
    private int orderId;
    private int documentId;
    private String pageName;
    private String barcode;
    private String pagePath;
    private int rotation;

    public Page(String pageId, int orderId, int documentId, String pageName, String pagePath, int rotation) {
        this.pageId = pageId;
        this.orderId = orderId;
        this.documentId = documentId;
        this.pageName =pageName;
        this.pagePath = pagePath;
        this.rotation = rotation;
    }

    public String getPageId() {
        return pageId;
    }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDocumentId() {
        return documentId;
    }
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getPageName() {
        return pageName;
    }

    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPagePath() {
        return pagePath;
    }

    public int getRotation() {
        return rotation;
    }
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

}