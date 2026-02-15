package com.bear.account.demo;

import com.bear.generated.withdraw.BearValue;
import com.bear.generated.withdraw.IdempotencyPort;
import com.bear.generated.withdraw.LedgerPort;
import com.bear.generated.withdraw.Withdraw;
import com.bear.generated.withdraw.WithdrawImpl;
import com.bear.generated.withdraw.WithdrawRequest;
import com.bear.generated.withdraw.WithdrawResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @Test
    void withdrawProducesExpectedBalance() {
        InMemoryIdempotencyPort idempotency = new InMemoryIdempotencyPort();
        InMemoryLedgerPort ledger = new InMemoryLedgerPort(new BigDecimal("100"));
        Withdraw withdraw = new Withdraw(idempotency, ledger, new WithdrawImpl());

        WithdrawRequest request = new WithdrawRequest("acc-1", new BigDecimal("10"), "USD", "tx-1");
        WithdrawResult result = withdraw.execute(request);

        assertEquals(new BigDecimal("90"), result.getBalance());
    }

    private static final class InMemoryLedgerPort implements LedgerPort {
        private BigDecimal balance;

        private InMemoryLedgerPort(BigDecimal initialBalance) {
            this.balance = initialBalance;
        }

        @Override
        public BearValue getBalance(BearValue input) {
            return BearValue.builder().put("balance", balance.toString()).build();
        }

        @Override
        public BearValue setBalance(BearValue input) {
            this.balance = new BigDecimal(input.get("balance"));
            return BearValue.empty();
        }
    }

    private static final class InMemoryIdempotencyPort implements IdempotencyPort {
        private final Map<String, BearValue> store = new HashMap<>();

        @Override
        public BearValue get(BearValue input) {
            BearValue found = store.get(input.get("key"));
            return found == null ? BearValue.empty() : found;
        }

        @Override
        public BearValue put(BearValue input) {
            store.put(input.get("key"), input);
            return BearValue.empty();
        }
    }
}
