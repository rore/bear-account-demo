package blocks.transaction.log.impl;

import com.bear.generated.transaction.log.BearValue;
import com.bear.generated.transaction.log.TransactionLogLogic;
import com.bear.generated.transaction.log.TransactionLog_AppendTransactionRequest;
import com.bear.generated.transaction.log.TransactionLog_AppendTransactionResult;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsRequest;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsResult;
import com.bear.generated.transaction.log.TransactionStorePort;

public final class TransactionLogImpl implements TransactionLogLogic {
    @Override
    public TransactionLog_AppendTransactionResult executeAppendTransaction(TransactionLog_AppendTransactionRequest request, TransactionStorePort transactionStorePort) {
        requireNonBlank(request.getAccountId(), "accountId");
        requireNonBlank(request.getType(), "type");
        requireNonBlank(request.getRequestId(), "requestId");
        if (request.getAmountCents() == null || request.getAmountCents() <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        if (request.getBalanceAfterCents() == null || request.getBalanceAfterCents() < 0) {
            throw new IllegalArgumentException("balanceAfterCents must be >= 0");
        }

        BearValue stored = transactionStorePort.append(BearValue.builder()
            .put("accountId", request.getAccountId())
            .put("type", request.getType())
            .put("requestId", request.getRequestId())
            .put("amountCents", Integer.toString(request.getAmountCents()))
            .put("balanceAfterCents", Integer.toString(request.getBalanceAfterCents()))
            .build());
        return new TransactionLog_AppendTransactionResult(parseInt(stored.get("txSeq"), "txSeq"));
    }

    @Override
    public TransactionLog_GetTransactionsResult executeGetTransactions(TransactionLog_GetTransactionsRequest request, TransactionStorePort transactionStorePort) {
        requireNonBlank(request.getAccountId(), "accountId");
        if (request.getSinceSeq() == null || request.getSinceSeq() < 0) {
            throw new IllegalArgumentException("sinceSeq must be >= 0");
        }

        BearValue listed = transactionStorePort.list(BearValue.builder()
            .put("accountId", request.getAccountId())
            .put("sinceSeq", Integer.toString(request.getSinceSeq()))
            .build());
        return new TransactionLog_GetTransactionsResult(defaultJson(listed.get("transactionsJson")));
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static int parseInt(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(field + " is missing");
        }
        return Integer.parseInt(raw);
    }

    private static String defaultJson(String json) {
        return json == null ? "[]" : json;
    }
}