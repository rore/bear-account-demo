package blocks._shared.state;

public final class TransactionRecord {
    private final int seq;
    private final String type;
    private final String requestId;
    private final int amountCents;
    private final int balanceAfterCents;

    public TransactionRecord(int seq, String type, String requestId, int amountCents, int balanceAfterCents) {
        this.seq = seq;
        this.type = type;
        this.requestId = requestId;
        this.amountCents = amountCents;
        this.balanceAfterCents = balanceAfterCents;
    }

    public int getSeq() {
        return seq;
    }

    public String getType() {
        return type;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public int getBalanceAfterCents() {
        return balanceAfterCents;
    }
}