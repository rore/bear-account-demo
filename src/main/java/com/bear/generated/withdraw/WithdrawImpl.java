package com.bear.generated.withdraw;

import java.math.BigDecimal;

public final class WithdrawImpl implements WithdrawLogic {
    @Override
    public WithdrawResult execute(WithdrawRequest request, IdempotencyPort idempotencyPort, LedgerPort ledgerPort) {
        // TODO: implement business logic.
        return new WithdrawResult(BigDecimal.ZERO);
    }
    // USER_EDIT_MARKER_DO_NOT_OVERWRITE
}
