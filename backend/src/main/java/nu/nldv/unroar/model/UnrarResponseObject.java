package nu.nldv.unroar.model;

public class UnrarResponseObject {
    private int queueId;

    public UnrarResponseObject(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }
}
