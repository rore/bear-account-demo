package blocks.account.impl;

public final class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("account not found: " + accountId);
    }
}