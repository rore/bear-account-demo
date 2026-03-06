package blocks._shared.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BankMemoryState {
    private final Map<String, AccountRecord> accounts = new HashMap<>();
    private final Map<String, List<TransactionRecord>> transactionsByAccount = new HashMap<>();
    private final Map<String, Map<String, String>> idempotency = new HashMap<>();

    public AccountRecord getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public AccountRecord putAccount(AccountRecord record) {
        accounts.put(record.getAccountId(), record);
        return record;
    }

    public Map<String, String> getIdempotencyValue(String key) {
        return idempotency.get(key);
    }

    public void putIdempotencyValue(String key, Map<String, String> payload) {
        idempotency.put(key, new HashMap<>(payload));
    }

    public int appendTransaction(String accountId, String type, String requestId, int amountCents, int balanceAfterCents) {
        List<TransactionRecord> existing = transactionsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
        int nextSeq = existing.size() + 1;
        existing.add(new TransactionRecord(nextSeq, type, requestId, amountCents, balanceAfterCents));
        return nextSeq;
    }

    public List<TransactionRecord> getTransactionsSince(String accountId, int sinceSeq) {
        List<TransactionRecord> existing = transactionsByAccount.getOrDefault(accountId, List.of());
        List<TransactionRecord> filtered = new ArrayList<>();
        for (TransactionRecord record : existing) {
            if (record.getSeq() > sinceSeq) {
                filtered.add(record);
            }
        }
        return filtered;
    }
}