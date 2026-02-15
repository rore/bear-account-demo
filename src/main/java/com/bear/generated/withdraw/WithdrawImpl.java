package com.bear.generated.withdraw;

import java.math.BigDecimal;

public final class WithdrawImpl implements WithdrawLogic {
    @Override
    public WithdrawResult execute(WithdrawRequest request, IdempotencyPort idempotencyPort, LedgerPort ledgerPort) {
        BearValue current = ledgerPort.getBalance(BearValue.builder()
            .put("accountId", request.getAccountId())
            .put("currency", request.getCurrency())
            .build());
        BigDecimal currentBalance = new BigDecimal(current.get("balance"));
        BigDecimal newBalance = currentBalance.subtract(request.getAmount());

        ledgerPort.setBalance(BearValue.builder()
            .put("accountId", request.getAccountId())
            .put("currency", request.getCurrency())
            .put("balance", newBalance.toString())
            .build());

        return new WithdrawResult(newBalance);
    }
}
