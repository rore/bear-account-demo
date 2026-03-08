package blocks.account.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

import com.bear.generated.account.AccountStorePort;
import com.bear.generated.account.BearValue;

public final class InMemoryAccountStorePort implements AccountStorePort {
    private final Map<String, AccountRecord> accounts = new LinkedHashMap<>();

    @Override
    public BearValue create(BearValue input) {
        String accountId = required(input, "accountId");
        AccountRecord record = new AccountRecord(
            accountId,
            input.get("ownerId"),
            Integer.parseInt(required(input, "balanceCents")));
        accounts.put(accountId, record);
        return toBearValue(record);
    }

    @Override
    public BearValue get(BearValue input) {
        AccountRecord record = accounts.get(required(input, "accountId"));
        return record == null ? BearValue.empty() : toBearValue(record);
    }

    @Override
    public BearValue put(BearValue input) {
        String accountId = required(input, "accountId");
        AccountRecord record = new AccountRecord(
            accountId,
            input.get("ownerId"),
            Integer.parseInt(required(input, "balanceCents")));
        accounts.put(accountId, record);
        return toBearValue(record);
    }

    private static BearValue toBearValue(AccountRecord record) {
        BearValue.Builder builder = BearValue.builder()
            .put("accountId", record.accountId())
            .put("balanceCents", Integer.toString(record.balanceCents()));
        if (record.ownerId() != null) {
            builder.put("ownerId", record.ownerId());
        }
        return builder.build();
    }

    private static String required(BearValue input, String field) {
        String value = input == null ? null : input.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private record AccountRecord(String accountId, String ownerId, int balanceCents) {
    }
}