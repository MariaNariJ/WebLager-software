package dk.easv.bll;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;

public class BarcodeService {

    public String scanBarcode(BufferedImage image) {
        try {
            if (image == null) {
                return "Unsupported file format or unreadable image.";
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);

            return result.getText();

        } catch (NotFoundException e) {
            return "No barcode found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}