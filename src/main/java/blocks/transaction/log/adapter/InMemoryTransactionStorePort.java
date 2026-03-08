package blocks.transaction.log.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bear.generated.transaction.log.BearValue;
import com.bear.generated.transaction.log.TransactionStorePort;

public final class InMemoryTransactionStorePort implements TransactionStorePort {
    private final Map<String, List<TransactionRecord>> transactionsByAccount = new LinkedHashMap<>();

    @Override
    public BearValue append(BearValue input) {
        String accountId = required(input, "accountId");
        List<TransactionRecord> transactions = transactionsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
        int nextSeq = transactions.size() + 1;
        transactions.add(new TransactionRecord(
            nextSeq,
            required(input, "type"),
            required(input, "requestId"),
            Integer.parseInt(required(input, "amountCents")),
            Integer.parseInt(required(input, "balanceAfterCents"))));
        return BearValue.builder().put("txSeq", Integer.toString(nextSeq)).build();
    }

    @Override
    public BearValue list(BearValue input) {
        String accountId = required(input, "accountId");
        int sinceSeq = Integer.parseInt(required(input, "sinceSeq"));
        List<TransactionRecord> transactions = transactionsByAccount.getOrDefault(accountId, List.of());
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (TransactionRecord transaction : transactions) {
            if (transaction.seq() <= sinceSeq) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('{')
                .append("\"seq\":").append(transaction.seq())
                .append(",\"type\":\"").append(escape(transaction.type())).append('\"')
                .append(",\"requestId\":\"").append(escape(transaction.requestId())).append('\"')
                .append(",\"amountCents\":").append(transaction.amountCents())
                .append(",\"balanceAfterCents\":").append(transaction.balanceAfterCents())
                .append('}');
        }
        json.append(']');
        return BearValue.builder().put("transactionsJson", json.toString()).build();
    }

    private static String required(BearValue input, String field) {
        String value = input == null ? null : input.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record TransactionRecord(int seq, String type, String requestId, int amountCents, int balanceAfterCents) {
    }
}