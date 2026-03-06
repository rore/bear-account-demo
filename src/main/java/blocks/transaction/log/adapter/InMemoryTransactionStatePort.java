package blocks.transaction.log.adapter;

import java.util.List;

import blocks._shared.state.BankMemoryState;
import blocks._shared.state.TransactionRecord;
import com.bear.generated.transaction.log.BearValue;
import com.bear.generated.transaction.log.TransactionStatePort;

public final class InMemoryTransactionStatePort implements TransactionStatePort {
    private final BankMemoryState state;

    public InMemoryTransactionStatePort(BankMemoryState state) {
        this.state = state;
    }

    @Override
    public BearValue get(BearValue input) {
        String accountId = input.get("accountId");
        String sinceSeqValue = input.get("sinceSeq");
        int sinceSeq = sinceSeqValue == null ? 0 : Integer.parseInt(sinceSeqValue);
        List<TransactionRecord> records = state.getTransactionsSince(accountId, sinceSeq);
        return BearValue.builder()
            .put("transactionsJson", toTransactionsJson(records))
            .build();
    }

    @Override
    public BearValue put(BearValue input) {
        int txSeq = state.appendTransaction(
            input.get("accountId"),
            input.get("type"),
            input.get("requestId"),
            Integer.parseInt(input.get("amountCents")),
            Integer.parseInt(input.get("balanceAfterCents"))
        );
        return BearValue.builder().put("txSeq", Integer.toString(txSeq)).build();
    }

    private String toTransactionsJson(List<TransactionRecord> records) {
        StringBuilder json = new StringBuilder();
        json.append("{\"transactions\":[");
        for (int i = 0; i < records.size(); i++) {
            TransactionRecord record = records.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append('{')
                .append("\"seq\":").append(record.getSeq()).append(',')
                .append("\"type\":\"").append(escape(record.getType())).append("\",")
                .append("\"requestId\":\"").append(escape(record.getRequestId())).append("\",")
                .append("\"amountCents\":").append(record.getAmountCents()).append(',')
                .append("\"balanceAfterCents\":").append(record.getBalanceAfterCents())
                .append('}');
        }
        json.append("]}");
        return json.toString();
    }

    private String escape(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}