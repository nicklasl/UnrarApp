package nu.nldv.unroar.model;

public class UnrarStatus {
    private final String fileName;
    private boolean queued;
    private int percentDone = 0;

    public UnrarStatus(String fileName, int percentDone) {
        this.fileName = fileName;
        this.percentDone = percentDone;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
