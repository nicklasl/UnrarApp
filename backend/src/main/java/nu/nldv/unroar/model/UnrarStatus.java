package nu.nldv.unroar.model;

public class UnrarStatus {
    private final String fileName;
    private int percentDone = 0;

    public UnrarStatus(String fileName, int percentDone) {
        this.fileName = fileName;
        this.percentDone = percentDone;
    }

    public String getFileName() {
        return fileName;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
