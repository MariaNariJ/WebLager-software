package dk.easv.be;

public class File {
    private String name;
    private String path;
    private long size;

    public File(String name, String path, long size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }

    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public long getSize() {
        return size;
    }
}
