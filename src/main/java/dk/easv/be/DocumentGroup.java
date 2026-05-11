package dk.easv.be;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a grouped document containing scanned pages.
 */
public class DocumentGroup {

    // Display title shown in the UI
    private String title;

    // Barcode identifying the document
    private String barcode;

    // Pages belonging to the document
    private List<Page> pages;

    public DocumentGroup(String title, String barcode) {
        this.title = title;
        this.barcode = barcode;
        this.pages = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getBarcode() {
        return barcode;
    }

    public List<Page> getPages() {
        return pages;
    }

    /**
     * Adds a page to the document.
     */
    public void addPage(Page page) {
        pages.add(page);
    }
}