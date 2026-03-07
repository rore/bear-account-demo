package blocks.account.impl;

import java.util.UUID;

import com.bear.generated.account.AccountLogic;
import com.bear.generated.account.AccountStorePort;
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
    public Account_CreateAccountResult executeCreateAccount(Account_CreateAccountRequest request, AccountStorePort accountStorePort) {
        requireNonBlank(request.getOwnerId(), "ownerId");

        String accountId = UUID.randomUUID().toString();
        accountStorePort.create(BearValue.builder()
            .put("accountId", accountId)
            .put("ownerId", request.getOwnerId())
            .put("balanceCents", "0")
            .build());
        return new Account_CreateAccountResult(accountId);
    }

    @Override
    public Account_DepositResult executeDeposit(Account_DepositRequest request, AccountStorePort accountStorePort, TransactionLogPort transactionLogPort) {
        int amountCents = requirePositiveAmount(request.getAmountCents());
        requireNonBlank(request.getRequestId(), "requestId");
        AccountState account = loadAccount(request.getAccountId(), accountStorePort);

        int balanceAfterCents = account.balanceCents() + amountCents;
        accountStorePort.put(toAccountValue(account.accountId(), account.ownerId(), balanceAfterCents));
        int txSeq = appendTransaction(transactionLogPort, account.accountId(), "DEPOSIT", request.getRequestId(), amountCents, balanceAfterCents);
        return new Account_DepositResult(balanceAfterCents, txSeq);
    }

    @Override
    public Account_GetBalanceResult executeGetBalance(Account_GetBalanceRequest request, AccountStorePort accountStorePort) {
        AccountState account = loadAccount(request.getAccountId(), accountStorePort);
        return new Account_GetBalanceResult(account.balanceCents());
    }

    @Override
    public Account_WithdrawResult executeWithdraw(Account_WithdrawRequest request, AccountStorePort accountStorePort, TransactionLogPort transactionLogPort) {
        int amountCents = requirePositiveAmount(request.getAmountCents());
        requireNonBlank(request.getRequestId(), "requestId");
        AccountState account = loadAccount(request.getAccountId(), accountStorePort);
        if (account.balanceCents() < amountCents) {
            throw new InsufficientFundsException(account.accountId());
        }

        int balanceAfterCents = account.balanceCents() - amountCents;
        accountStorePort.put(toAccountValue(account.accountId(), account.ownerId(), balanceAfterCents));
        int txSeq = appendTransaction(transactionLogPort, account.accountId(), "WITHDRAW", request.getRequestId(), amountCents, balanceAfterCents);
        return new Account_WithdrawResult(balanceAfterCents, txSeq);
    }

    private static AccountState loadAccount(String accountId, AccountStorePort accountStorePort) {
        requireNonBlank(accountId, "accountId");
        BearValue stored = accountStorePort.get(BearValue.builder().put("accountId", accountId).build());
        if (stored.get("accountId") == null) {
            throw new AccountNotFoundException(accountId);
        }
        return new AccountState(
            stored.get("accountId"),
            stored.get("ownerId"),
            parseInt(stored.get("balanceCents"), "balanceCents"));
    }

    private static BearValue toAccountValue(String accountId, String ownerId, int balanceCents) {
        return BearValue.builder()
            .put("accountId", accountId)
            .put("ownerId", ownerId)
            .put("balanceCents", Integer.toString(balanceCents))
            .build();
    }

    private static int appendTransaction(
        TransactionLogPort transactionLogPort,
        String accountId,
        String type,
        String requestId,
        int amountCents,
        int balanceAfterCents
    ) {
        BearValue result = transactionLogPort.call(BearValue.builder()
            .put("op", "AppendTransaction")
            .put("accountId", accountId)
            .put("type", type)
            .put("requestId", requestId)
            .put("amountCents", Integer.toString(amountCents))
            .put("balanceAfterCents", Integer.toString(balanceAfterCents))
            .build());
        return parseInt(result.get("txSeq"), "txSeq");
    }

    private static int requirePositiveAmount(Integer amountCents) {
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        return amountCents;
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

    private record AccountState(String accountId, String ownerId, int balanceCents) {
    }
}