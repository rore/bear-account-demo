package com.bear.generated.withdraw;

import java.math.BigDecimal;

public final class WithdrawImpl implements WithdrawLogic {
    @Override
    public WithdrawResult execute(WithdrawRequest request, IdempotencyPort idempotencyPort, LedgerPort ledgerPort) {
        // Naive variant: intentionally wrong behavior for demo proof.
        return new WithdrawResult(new BigDecimal("-1"));
    }
}
