package com.bear.generated.withdraw;

import java.math.BigDecimal;

public final class WithdrawImpl implements WithdrawLogic {
    @Override
    public WithdrawResult execute(WithdrawRequest request, IdempotencyPort idempotencyPort, LedgerPort ledgerPort) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getAccountId() == null || request.getAccountId().isBlank()) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        BearValue currentBalanceValue = ledgerPort.getBalance(
                BearValue.builder().put("accountId", request.getAccountId()).build());
        if (currentBalanceValue == null || currentBalanceValue.get("balance") == null) {
            throw new IllegalStateException("ledger.getBalance did not return balance");
        }

        BigDecimal currentBalance = new BigDecimal(currentBalanceValue.get("balance"));
        BigDecimal resultingBalance = currentBalance.subtract(request.getAmount());
        if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("insufficient funds");
        }

        ledgerPort.setBalance(BearValue.builder()
                .put("accountId", request.getAccountId())
                .put("balance", resultingBalance.toPlainString())
                .build());

        return new WithdrawResult(resultingBalance);
    }
}
