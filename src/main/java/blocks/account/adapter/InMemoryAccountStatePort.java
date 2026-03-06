package blocks.account.adapter;

import blocks._shared.state.AccountRecord;
import blocks._shared.state.BankMemoryState;
import com.bear.generated.account.AccountStatePort;
import com.bear.generated.account.BearValue;

public final class InMemoryAccountStatePort implements AccountStatePort {
    private final BankMemoryState state;

    public InMemoryAccountStatePort(BankMemoryState state) {
        this.state = state;
    }

    @Override
    public BearValue get(BearValue input) {
        String accountId = input.get("accountId");
        AccountRecord record = state.getAccount(accountId);
        if (record == null) {
            return BearValue.empty();
        }
        return BearValue.builder()
            .put("accountId", record.getAccountId())
            .put("ownerId", record.getOwnerId())
            .put("balanceCents", Integer.toString(record.getBalanceCents()))
            .build();
    }

    @Override
    public BearValue put(BearValue input) {
        AccountRecord record = new AccountRecord(
            input.get("accountId"),
            input.get("ownerId"),
            Integer.parseInt(input.get("balanceCents"))
        );
        state.putAccount(record);
        return BearValue.builder()
            .put("accountId", record.getAccountId())
            .put("ownerId", record.getOwnerId())
            .put("balanceCents", Integer.toString(record.getBalanceCents()))
            .build();
    }
}