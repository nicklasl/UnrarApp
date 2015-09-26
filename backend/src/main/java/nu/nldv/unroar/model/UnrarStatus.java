package nu.nldv.unroar.model;

public class UnrarStatus {
    private boolean queued;
    private int percentDone = 0;

    public UnrarStatus(int percentDone) {
        this.percentDone = percentDone;
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
