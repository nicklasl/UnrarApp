package nu.nldv.uppackaren.model;

public class StatusResponse {
    private boolean queued;
    private int percentDone;

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public int getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }
}
