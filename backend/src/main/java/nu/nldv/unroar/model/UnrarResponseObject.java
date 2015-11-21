package nu.nldv.unroar.model;

public class UnrarResponseObject {
    private String queueId;

    public UnrarResponseObject(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
