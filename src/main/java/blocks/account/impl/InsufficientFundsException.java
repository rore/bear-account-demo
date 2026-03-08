package blocks.account.impl;

public final class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId) {
        super("insufficient funds for account: " + accountId);
    }
}