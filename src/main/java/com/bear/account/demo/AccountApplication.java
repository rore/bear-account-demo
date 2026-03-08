package com.bear.account.demo;

import blocks.account.adapter.InMemoryAccountStorePort;
import blocks.account.adapter.InMemoryIdempotencyPort;
import blocks.transaction.log.adapter.InMemoryTransactionStorePort;

import com.bear.generated.account.Account_CreateAccount;
import com.bear.generated.account.Account_CreateAccountRequest;
import com.bear.generated.account.Account_Deposit;
import com.bear.generated.account.Account_DepositRequest;
import com.bear.generated.account.Account_GetBalance;
import com.bear.generated.account.Account_GetBalanceRequest;
import com.bear.generated.account.Account_TransactionLogBlockClient;
import com.bear.generated.account.Account_Withdraw;
import com.bear.generated.account.Account_WithdrawRequest;
import com.bear.generated.account.TransactionLogPort;
import com.bear.generated.transaction.log.TransactionLog_AppendTransaction;
import com.bear.generated.transaction.log.TransactionLog_GetTransactions;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsRequest;

public final class AccountApplication {
    private final Account_CreateAccount createAccount;
    private final Account_Deposit deposit;
    private final Account_Withdraw withdraw;
    private final Account_GetBalance getBalance;
    private final TransactionLog_GetTransactions getTransactions;

    public AccountApplication() {
        InMemoryAccountStorePort accountStorePort = new InMemoryAccountStorePort();
        InMemoryIdempotencyPort idempotencyPort = new InMemoryIdempotencyPort();
        InMemoryTransactionStorePort transactionStorePort = new InMemoryTransactionStorePort();
        TransactionLog_AppendTransaction appendTransaction = TransactionLog_AppendTransaction.of(transactionStorePort);
        TransactionLogPort transactionLogPort = new Account_TransactionLogBlockClient(appendTransaction);

        this.createAccount = Account_CreateAccount.of(accountStorePort, idempotencyPort, transactionLogPort);
        this.deposit = Account_Deposit.of(accountStorePort, idempotencyPort, transactionLogPort);
        this.withdraw = Account_Withdraw.of(accountStorePort, idempotencyPort, transactionLogPort);
        this.getBalance = Account_GetBalance.of(accountStorePort, idempotencyPort, transactionLogPort);
        this.getTransactions = TransactionLog_GetTransactions.of(transactionStorePort);
    }

    public String createAccount(String ownerId) {
        return createAccount.execute(new Account_CreateAccountRequest(ownerId)).getAccountId();
    }

    public OperationResult deposit(String accountId, Integer amountCents, String requestId, String note) {
        return toOperationResult(deposit.execute(new Account_DepositRequest(accountId, amountCents, note, requestId)));
    }

    public OperationResult withdraw(String accountId, Integer amountCents, String requestId, String note) {
        return toOperationResult(withdraw.execute(new Account_WithdrawRequest(accountId, amountCents, note, requestId)));
    }

    public int getBalance(String accountId) {
        return getBalance.execute(new Account_GetBalanceRequest(accountId)).getBalanceCents();
    }

    public String getTransactionsJson(String accountId, Integer sinceSeq) {
        getBalance(accountId);
        return getTransactions.execute(new TransactionLog_GetTransactionsRequest(accountId, sinceSeq)).getTransactionsJson();
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
