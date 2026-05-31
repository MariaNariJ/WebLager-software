package dk.easv.bll.interfaces;

import dk.easv.be.Page;

import java.util.function.Consumer;

public interface IFileManager {

    void updatePageRotation(Page page);

    boolean scanNextDocument(
            String boxName,
            Consumer<Page> scannedPage);

    void resetLocalBoxScan();

    boolean hasMoreFiles();

    boolean localBoxExists(String boxName);
}