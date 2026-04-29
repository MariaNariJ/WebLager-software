package dk.easv.be;

public class Page {

    private String pageName;
    private String pagePath;
    private int pageId;
    private String barcode;
    private int rotation; // 0, 90, 180, 270

    public Page(String fileName, String filePath, int pageId) {
        this.pageName = fileName;
        this.pagePath = filePath;
        this.pageId = pageId;
    }

    public String getPageName() {
        return pageName;
    }

    public String getPagePath() {
        return pagePath;
    }

    public int getPageId() {
        return pageId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getRotation() { return rotation;}

    public void setRotation(int rotation) { this.rotation = rotation;}
}