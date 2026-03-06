package blocks.transaction.log.impl;

import com.bear.generated.transaction.log.BearValue;
import com.bear.generated.transaction.log.TransactionLogLogic;
import com.bear.generated.transaction.log.TransactionLog_AppendTransactionRequest;
import com.bear.generated.transaction.log.TransactionLog_AppendTransactionResult;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsRequest;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsResult;
import com.bear.generated.transaction.log.TransactionStatePort;

public final class TransactionLogImpl implements TransactionLogLogic {
    @Override
    public TransactionLog_AppendTransactionResult executeAppendTransaction(TransactionLog_AppendTransactionRequest request, TransactionStatePort transactionStatePort) {
        BearValue appended = transactionStatePort.put(BearValue.builder()
            .put("accountId", requireText(request.getAccountId(), "accountId"))
            .put("type", requireText(request.getType(), "type"))
            .put("requestId", requireText(request.getRequestId(), "requestId"))
            .put("amountCents", Integer.toString(requireNonNegative(request.getAmountCents(), "amountCents")))
            .put("balanceAfterCents", Integer.toString(requireNonNegative(request.getBalanceAfterCents(), "balanceAfterCents")))
            .build());
        return new TransactionLog_AppendTransactionResult(Integer.parseInt(appended.get("txSeq")));
    }

    @Override
    public TransactionLog_GetTransactionsResult executeGetTransactions(TransactionLog_GetTransactionsRequest request, TransactionStatePort transactionStatePort) {
        int sinceSeq = requireNonNegative(request.getSinceSeq(), "sinceSeq");
        BearValue result = transactionStatePort.get(BearValue.builder()
            .put("accountId", requireText(request.getAccountId(), "accountId"))
            .put("sinceSeq", Integer.toString(sinceSeq))
            .build());
        return new TransactionLog_GetTransactionsResult(result.get("transactionsJson"));
    }

    private int requireNonNegative(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}