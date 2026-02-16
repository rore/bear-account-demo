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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WithdrawFeatureTest {

    @Test
    void withdrawSuccessUpdatesBalanceOnce() {
        InMemoryIdempotencyPort idempotency = new InMemoryIdempotencyPort();
        InMemoryLedgerPort ledger = new InMemoryLedgerPort();
        ledger.balances.put("acct-1", new BigDecimal("100.00"));

        Withdraw withdraw = new Withdraw(idempotency, ledger, new WithdrawImpl());
        WithdrawResult result = withdraw.execute(
                new WithdrawRequest("acct-1", new BigDecimal("25.00"), "USD", "tx-1"));

        assertEquals(new BigDecimal("75.00"), result.getBalance());
        assertEquals(new BigDecimal("75.00"), ledger.balances.get("acct-1"));
        assertEquals(1, ledger.setBalanceCalls);
        assertEquals(1, ledger.getBalanceCalls);
    }

    @Test
    void withdrawRejectsOverdraftAndDoesNotWriteLedger() {
        InMemoryIdempotencyPort idempotency = new InMemoryIdempotencyPort();
        InMemoryLedgerPort ledger = new InMemoryLedgerPort();
        ledger.balances.put("acct-1", new BigDecimal("10.00"));

        Withdraw withdraw = new Withdraw(idempotency, ledger, new WithdrawImpl());
        assertThrows(IllegalArgumentException.class, () ->
                withdraw.execute(new WithdrawRequest("acct-1", new BigDecimal("25.00"), "USD", "tx-2")));

        assertEquals(new BigDecimal("10.00"), ledger.balances.get("acct-1"));
        assertEquals(0, ledger.setBalanceCalls);
    }

    @Test
    void idempotentReplayDoesNotDoubleApplyWithdrawal() {
        InMemoryIdempotencyPort idempotency = new InMemoryIdempotencyPort();
        InMemoryLedgerPort ledger = new InMemoryLedgerPort();
        ledger.balances.put("acct-1", new BigDecimal("100.00"));

        Withdraw withdraw = new Withdraw(idempotency, ledger, new WithdrawImpl());
        WithdrawRequest request = new WithdrawRequest("acct-1", new BigDecimal("25.00"), "USD", "tx-3");

        WithdrawResult first = withdraw.execute(request);
        WithdrawResult replay = withdraw.execute(request);

        assertEquals(new BigDecimal("75.00"), first.getBalance());
        assertEquals(new BigDecimal("75.00"), replay.getBalance());
        assertEquals(new BigDecimal("75.00"), ledger.balances.get("acct-1"));
        assertEquals(1, ledger.setBalanceCalls);
        assertNotNull(idempotency.store.get("tx-3"));
    }

    private static final class InMemoryIdempotencyPort implements IdempotencyPort {
        private final Map<String, BearValue> store = new HashMap<>();

        @Override
        public BearValue get(BearValue input) {
            String key = input.get("key");
            BearValue stored = store.get(key);
            if (stored == null) {
                return BearValue.builder().put("hit", "false").put("key", key).build();
            }
            return stored;
        }

        @Override
        public BearValue put(BearValue input) {
            String key = input.get("key");
            store.put(key, input);
            return input;
        }
    }

    private static final class InMemoryLedgerPort implements LedgerPort {
        private final Map<String, BigDecimal> balances = new HashMap<>();
        private int getBalanceCalls;
        private int setBalanceCalls;

        @Override
        public BearValue getBalance(BearValue input) {
            getBalanceCalls++;
            String accountId = input.get("accountId");
            BigDecimal balance = balances.get(accountId);
            return BearValue.builder()
                    .put("accountId", accountId)
                    .put("balance", balance == null ? null : balance.toPlainString())
                    .build();
        }

        @Override
        public BearValue setBalance(BearValue input) {
            setBalanceCalls++;
            String accountId = input.get("accountId");
            BigDecimal newBalance = new BigDecimal(input.get("balance"));
            balances.put(accountId, newBalance);
            return input;
        }
    }
}
