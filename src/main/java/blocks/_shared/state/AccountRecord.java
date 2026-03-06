package blocks._shared.state;

public final class AccountRecord {
    private final String accountId;
    private final String ownerId;
    private final int balanceCents;

    public AccountRecord(String accountId, String ownerId, int balanceCents) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.balanceCents = balanceCents;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getBalanceCents() {
        return balanceCents;
    }
}