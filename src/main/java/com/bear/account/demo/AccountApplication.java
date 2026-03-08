package com.bear.account.demo;

import blocks.account.adapter.InMemoryAccountStorePort;
import blocks.account.adapter.InMemoryIdempotencyPort;
import blocks.alert.log.adapter.InMemoryAlertStorePort;
import com.bear.generated.account.Account_AlertLogBlockClient;
import com.bear.generated.account.Account_CreateAccount;
import com.bear.generated.account.Account_CreateAccountRequest;
import com.bear.generated.account.Account_Deposit;
import com.bear.generated.account.Account_DepositRequest;
import com.bear.generated.account.Account_GetBalance;
import com.bear.generated.account.Account_GetBalanceRequest;
import com.bear.generated.account.Account_TransactionLogBlockClient;
import com.bear.generated.account.Account_Withdraw;
import com.bear.generated.account.Account_WithdrawRequest;
import com.bear.generated.account.AlertLogPort;
import com.bear.generated.account.TransactionLogPort;
import com.bear.generated.alert.log.AlertLog_AppendAlert;
import com.bear.generated.alert.log.AlertLog_GetAlerts;
import com.bear.generated.alert.log.AlertLog_GetAlertsRequest;
import com.bear.generated.transaction.log.TransactionLog_AppendTransaction;
import com.bear.generated.transaction.log.TransactionLog_GetTransactions;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsRequest;
import blocks.transaction.log.adapter.InMemoryTransactionStorePort;

public final class AccountApplication {
    private final Account_CreateAccount createAccount;
    private final Account_Deposit deposit;
    private final Account_Withdraw withdraw;
    private final Account_GetBalance getBalance;
    private final TransactionLog_GetTransactions getTransactions;
    private final AlertLog_GetAlerts getAlerts;

    public AccountApplication() {
        InMemoryAccountStorePort accountStorePort = new InMemoryAccountStorePort();
        InMemoryIdempotencyPort idempotencyPort = new InMemoryIdempotencyPort();
        InMemoryTransactionStorePort transactionStorePort = new InMemoryTransactionStorePort();
        InMemoryAlertStorePort alertStorePort = new InMemoryAlertStorePort();
        TransactionLog_AppendTransaction appendTransaction = TransactionLog_AppendTransaction.of(transactionStorePort);
        AlertLog_AppendAlert appendAlert = AlertLog_AppendAlert.of(alertStorePort);
        TransactionLogPort transactionLogPort = new Account_TransactionLogBlockClient(appendTransaction);
        AlertLogPort alertLogPort = new Account_AlertLogBlockClient(appendAlert);

        this.createAccount = Account_CreateAccount.of(accountStorePort, alertLogPort, idempotencyPort, transactionLogPort);
        this.deposit = Account_Deposit.of(accountStorePort, alertLogPort, idempotencyPort, transactionLogPort);
        this.withdraw = Account_Withdraw.of(accountStorePort, alertLogPort, idempotencyPort, transactionLogPort);
        this.getBalance = Account_GetBalance.of(accountStorePort, alertLogPort, idempotencyPort, transactionLogPort);
        this.getTransactions = TransactionLog_GetTransactions.of(transactionStorePort);
        this.getAlerts = AlertLog_GetAlerts.of(alertStorePort);
    }

    public String createAccount(String ownerId) {
        return createAccount.execute(new Account_CreateAccountRequest(ownerId)).getAccountId();
    }

    public OperationResult deposit(String accountId, Integer amountCents, String requestId) {
        return toOperationResult(deposit.execute(new Account_DepositRequest(accountId, amountCents, requestId)));
    }

    public OperationResult withdraw(String accountId, Integer amountCents, String requestId) {
        return toOperationResult(withdraw.execute(new Account_WithdrawRequest(accountId, amountCents, requestId)));
    }

    public int getBalance(String accountId) {
        return getBalance.execute(new Account_GetBalanceRequest(accountId)).getBalanceCents();
    }

    public String getTransactionsJson(String accountId, Integer sinceSeq) {
        getBalance(accountId);
        return getTransactions.execute(new TransactionLog_GetTransactionsRequest(accountId, sinceSeq)).getTransactionsJson();
    }

    public String getAlertsJson(String accountId) {
        getBalance(accountId);
        return getAlerts.execute(new AlertLog_GetAlertsRequest(accountId)).getAlertsJson();
    }

    private static OperationResult toOperationResult(com.bear.generated.account.Account_DepositResult result) {
        return new OperationResult(result.getBalanceCents(), result.getTxSeq());
    }

    private static OperationResult toOperationResult(com.bear.generated.account.Account_WithdrawResult result) {
        return new OperationResult(result.getBalanceCents(), result.getTxSeq());
    }

    public record OperationResult(int balanceCents, int txSeq) {
    }
}
