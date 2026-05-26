package dk.easv.be;

import java.sql.Date;

public class Document {

    private int id;

    private int BoxId;
    private String Barcode;
    private Date Date;
    private String DocumentName;
    private String DocumentType;

    public Document(
            int BoxId,
            String Barcode,
            Date Date,
            String DocumentName,
            String DocumentType
    ) {

        this.BoxId = BoxId;
        this.Barcode = Barcode;
        this.Date = Date;
        this.DocumentName = DocumentName;
        this.DocumentType = DocumentType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBoxId() {
        return BoxId;
    }

    public void setBoxId(int BoxId) {
        this.BoxId = BoxId;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String Barcode) {
        this.Barcode = Barcode;
    }

    public Date getDate() {
        return Date;
    }

    public String getDocumentName() {
        return DocumentName;
    }

    public String getDocumentType() {
        return DocumentType;
    }
}