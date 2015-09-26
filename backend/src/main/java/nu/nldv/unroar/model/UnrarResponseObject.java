package nu.nldv.unroar.model;

public class UnrarResponseObject {
    private String filePath;

    public UnrarResponseObject(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
