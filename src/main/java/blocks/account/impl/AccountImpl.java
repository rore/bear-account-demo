package blocks.account.impl;

import java.util.UUID;

import com.bear.generated.account.AccountLogic;
import com.bear.generated.account.AccountStatePort;
import com.bear.generated.account.Account_CreateAccountRequest;
import com.bear.generated.account.Account_CreateAccountResult;
import com.bear.generated.account.Account_DepositRequest;
import com.bear.generated.account.Account_DepositResult;
import com.bear.generated.account.Account_GetBalanceRequest;
import com.bear.generated.account.Account_GetBalanceResult;
import com.bear.generated.account.Account_WithdrawRequest;
import com.bear.generated.account.Account_WithdrawResult;
import com.bear.generated.account.BearValue;
import com.bear.generated.account.TransactionLogPort;

public final class AccountImpl implements AccountLogic {
    @Override
    public Account_CreateAccountResult executeCreateAccount(Account_CreateAccountRequest request, AccountStatePort accountStatePort) {
        String ownerId = requireText(request.getOwnerId(), "ownerId");
        String accountId = UUID.randomUUID().toString();
        accountStatePort.put(BearValue.builder()
            .put("accountId", accountId)
            .put("ownerId", ownerId)
            .put("balanceCents", "0")
            .build());
        return new Account_CreateAccountResult(accountId);
    }

    @Override
    public Account_DepositResult executeDeposit(Account_DepositRequest request, AccountStatePort accountStatePort, TransactionLogPort transactionLogPort) {
        int amountCents = requirePositiveAmount(request.getAmountCents());
        String requestId = requireText(request.getRequestId(), "requestId");
        BearValue existing = loadAccount(accountStatePort, request.getAccountId());
        int newBalance = Integer.parseInt(existing.get("balanceCents")) + amountCents;
        int txSeq = appendTransaction(transactionLogPort, request.getAccountId(), requestId, amountCents, newBalance, "DEPOSIT");
        accountStatePort.put(BearValue.builder()
            .put("accountId", existing.get("accountId"))
            .put("ownerId", existing.get("ownerId"))
            .put("balanceCents", Integer.toString(newBalance))
            .build());
        return new Account_DepositResult(newBalance, txSeq);
    }

    @Override
    public Account_GetBalanceResult executeGetBalance(Account_GetBalanceRequest request, AccountStatePort accountStatePort) {
        BearValue existing = loadAccount(accountStatePort, request.getAccountId());
        return new Account_GetBalanceResult(Integer.parseInt(existing.get("balanceCents")));
    }

    @Override
    public Account_WithdrawResult executeWithdraw(Account_WithdrawRequest request, AccountStatePort accountStatePort, TransactionLogPort transactionLogPort) {
        int amountCents = requirePositiveAmount(request.getAmountCents());
        String requestId = requireText(request.getRequestId(), "requestId");
        BearValue existing = loadAccount(accountStatePort, request.getAccountId());
        int currentBalance = Integer.parseInt(existing.get("balanceCents"));
        if (currentBalance < amountCents) {
            throw new InsufficientFundsException(request.getAccountId());
        }
        int newBalance = currentBalance - amountCents;
        int txSeq = appendTransaction(transactionLogPort, request.getAccountId(), requestId, amountCents, newBalance, "WITHDRAW");
        accountStatePort.put(BearValue.builder()
            .put("accountId", existing.get("accountId"))
            .put("ownerId", existing.get("ownerId"))
            .put("balanceCents", Integer.toString(newBalance))
            .build());
        return new Account_WithdrawResult(newBalance, txSeq);
    }

    private BearValue loadAccount(AccountStatePort accountStatePort, String accountId) {
        String resolvedAccountId = requireText(accountId, "accountId");
        BearValue existing = accountStatePort.get(BearValue.builder().put("accountId", resolvedAccountId).build());
        if (existing.get("accountId") == null) {
            throw new AccountNotFoundException(resolvedAccountId);
        }
        return existing;
    }

    private int appendTransaction(TransactionLogPort transactionLogPort, String accountId, String requestId, int amountCents, int balanceAfterCents, String type) {
        BearValue appended = transactionLogPort.call(BearValue.builder()
            .put("op", "AppendTransaction")
            .put("accountId", accountId)
            .put("amountCents", Integer.toString(amountCents))
            .put("balanceAfterCents", Integer.toString(balanceAfterCents))
            .put("requestId", requestId)
            .put("type", type)
            .build());
        return Integer.parseInt(appended.get("txSeq"));
    }

    private int requirePositiveAmount(Integer amountCents) {
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        return amountCents;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}