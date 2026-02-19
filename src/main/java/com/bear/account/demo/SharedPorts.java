package com.bear.account.demo;

import com.bear.generated.accountservice.WorkQueuePort;
import com.bear.generated.scheduledtransferworker.ScheduledTransferWorker;
import com.bear.generated.scheduledtransferworker.ScheduledTransferWorkerRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class SharedPorts {
    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private final Map<String, ScheduleRecord> schedules = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> dailyUsed = new ConcurrentHashMap<>();
    private final Map<String, com.bear.generated.accountservice.BearValue> accountIdempotency = new ConcurrentHashMap<>();
    private final Map<String, com.bear.generated.scheduledtransferworker.BearValue> workerIdempotency = new ConcurrentHashMap<>();
    private final List<Map<String, String>> auditEvents = new CopyOnWriteArrayList<>();
    private final ExecutorService workerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("scheduled-transfer-worker");
        thread.setDaemon(true);
        return thread;
    });

    private volatile ScheduledTransferWorker scheduledTransferWorker;

    private final com.bear.generated.accountservice.AccountStorePort accountServiceAccountStorePort = new com.bear.generated.accountservice.AccountStorePort() {
        @Override
        public com.bear.generated.accountservice.BearValue getBalance(com.bear.generated.accountservice.BearValue input) {
            BigDecimal balance = balances.getOrDefault(safe(input.get("accountId")), BigDecimal.ZERO);
            return com.bear.generated.accountservice.BearValue.builder().put("balance", balance.toPlainString()).build();
        }

        @Override
        public com.bear.generated.accountservice.BearValue putBalance(com.bear.generated.accountservice.BearValue input) {
            balances.put(safe(input.get("accountId")), parseDecimal(input.get("balance")));
            return com.bear.generated.accountservice.BearValue.empty();
        }
    };

    private final com.bear.generated.accountservice.AuditPort accountServiceAuditPort = input -> {
        auditEvents.add(input.asMap());
        return com.bear.generated.accountservice.BearValue.empty();
    };

    private final com.bear.generated.accountservice.IdempotencyPort accountServiceIdempotencyPort = new com.bear.generated.accountservice.IdempotencyPort() {
        @Override
        public com.bear.generated.accountservice.BearValue get(com.bear.generated.accountservice.BearValue input) {
            return accountIdempotency.get(safe(input.get("key")));
        }

        @Override
        public com.bear.generated.accountservice.BearValue put(com.bear.generated.accountservice.BearValue input) {
            accountIdempotency.put(safe(input.get("key")), input);
            return com.bear.generated.accountservice.BearValue.empty();
        }
    };

    private final com.bear.generated.accountservice.ScheduleStorePort accountServiceScheduleStorePort = input -> {
        String scheduleId = safe(input.get("scheduleId"));
        schedules.put(scheduleId, new ScheduleRecord(
            scheduleId,
            safe(input.get("sourceAccountId")),
            safe(input.get("targetAccountId")),
            parseDecimal(input.get("amount")),
            safe(input.get("scheduleDate"))
        ));
        return com.bear.generated.accountservice.BearValue.empty();
    };

    private final WorkQueuePort accountServiceWorkQueuePort = input -> {
        String scheduleId = safe(input.get("scheduleId"));
        ScheduledTransferWorker worker = this.scheduledTransferWorker;
        if (worker != null && !scheduleId.isBlank()) {
            workerExecutor.submit(() -> worker.execute(new ScheduledTransferWorkerRequest(scheduleId, "worker:" + scheduleId)));
        }
        return com.bear.generated.accountservice.BearValue.empty();
    };

    private final com.bear.generated.scheduledtransferworker.AccountStorePort workerAccountStorePort = new com.bear.generated.scheduledtransferworker.AccountStorePort() {
        @Override
        public com.bear.generated.scheduledtransferworker.BearValue getBalance(com.bear.generated.scheduledtransferworker.BearValue input) {
            BigDecimal balance = balances.getOrDefault(safe(input.get("accountId")), BigDecimal.ZERO);
            return com.bear.generated.scheduledtransferworker.BearValue.builder().put("balance", balance.toPlainString()).build();
        }

        @Override
        public com.bear.generated.scheduledtransferworker.BearValue putBalance(com.bear.generated.scheduledtransferworker.BearValue input) {
            balances.put(safe(input.get("accountId")), parseDecimal(input.get("balance")));
            return com.bear.generated.scheduledtransferworker.BearValue.empty();
        }
    };

    private final com.bear.generated.scheduledtransferworker.AuditPort workerAuditPort = input -> {
        auditEvents.add(input.asMap());
        return com.bear.generated.scheduledtransferworker.BearValue.empty();
    };

    private final com.bear.generated.scheduledtransferworker.DailyLimitStorePort workerDailyLimitStorePort = new com.bear.generated.scheduledtransferworker.DailyLimitStorePort() {
        @Override
        public com.bear.generated.scheduledtransferworker.BearValue getUsed(com.bear.generated.scheduledtransferworker.BearValue input) {
            String key = dailyUsageKey(input.get("accountId"), input.get("day"));
            BigDecimal used = dailyUsed.getOrDefault(key, BigDecimal.ZERO);
            return com.bear.generated.scheduledtransferworker.BearValue.builder().put("used", used.toPlainString()).build();
        }

        @Override
        public com.bear.generated.scheduledtransferworker.BearValue putUsed(com.bear.generated.scheduledtransferworker.BearValue input) {
            dailyUsed.put(dailyUsageKey(input.get("accountId"), input.get("day")), parseDecimal(input.get("used")));
            return com.bear.generated.scheduledtransferworker.BearValue.empty();
        }
    };

    private final com.bear.generated.scheduledtransferworker.IdempotencyPort workerIdempotencyPort = new com.bear.generated.scheduledtransferworker.IdempotencyPort() {
        @Override
        public com.bear.generated.scheduledtransferworker.BearValue get(com.bear.generated.scheduledtransferworker.BearValue input) {
            return workerIdempotency.get(safe(input.get("key")));
        }

        @Override
        public com.bear.generated.scheduledtransferworker.BearValue put(com.bear.generated.scheduledtransferworker.BearValue input) {
            workerIdempotency.put(safe(input.get("key")), input);
            return com.bear.generated.scheduledtransferworker.BearValue.empty();
        }
    };

    private final com.bear.generated.scheduledtransferworker.ScheduleStorePort workerScheduleStorePort = new com.bear.generated.scheduledtransferworker.ScheduleStorePort() {
        @Override
        public com.bear.generated.scheduledtransferworker.BearValue get(com.bear.generated.scheduledtransferworker.BearValue input) {
            ScheduleRecord record = schedules.get(safe(input.get("scheduleId")));
            if (record == null) {
                return null;
            }
            return com.bear.generated.scheduledtransferworker.BearValue.builder()
                .put("scheduleId", record.scheduleId)
                .put("sourceAccountId", record.sourceAccountId)
                .put("targetAccountId", record.targetAccountId)
                .put("amount", record.amount.toPlainString())
                .put("scheduleDate", record.scheduleDate)
                .put("status", record.status)
                .build();
        }

        @Override
        public com.bear.generated.scheduledtransferworker.BearValue markExecuted(com.bear.generated.scheduledtransferworker.BearValue input) {
            ScheduleRecord record = schedules.get(safe(input.get("scheduleId")));
            if (record != null) {
                record.status = "EXECUTED";
                record.attempts = record.attempts + 1;
            }
            return com.bear.generated.scheduledtransferworker.BearValue.empty();
        }

        @Override
        public com.bear.generated.scheduledtransferworker.BearValue markFailed(com.bear.generated.scheduledtransferworker.BearValue input) {
            ScheduleRecord record = schedules.get(safe(input.get("scheduleId")));
            if (record != null) {
                record.status = "FAILED";
                record.attempts = record.attempts + 1;
                record.failureReason = safe(input.get("reason"));
            }
            return com.bear.generated.scheduledtransferworker.BearValue.empty();
        }
    };

    public com.bear.generated.accountservice.AccountStorePort accountServiceAccountStorePort() {
        return accountServiceAccountStorePort;
    }

    public com.bear.generated.accountservice.AuditPort accountServiceAuditPort() {
        return accountServiceAuditPort;
    }

    public com.bear.generated.accountservice.IdempotencyPort accountServiceIdempotencyPort() {
        return accountServiceIdempotencyPort;
    }

    public com.bear.generated.accountservice.ScheduleStorePort accountServiceScheduleStorePort() {
        return accountServiceScheduleStorePort;
    }

    public WorkQueuePort accountServiceWorkQueuePort() {
        return accountServiceWorkQueuePort;
    }

    public com.bear.generated.scheduledtransferworker.AccountStorePort workerAccountStorePort() {
        return workerAccountStorePort;
    }

    public com.bear.generated.scheduledtransferworker.AuditPort workerAuditPort() {
        return workerAuditPort;
    }

    public com.bear.generated.scheduledtransferworker.DailyLimitStorePort workerDailyLimitStorePort() {
        return workerDailyLimitStorePort;
    }

    public com.bear.generated.scheduledtransferworker.IdempotencyPort workerIdempotencyPort() {
        return workerIdempotencyPort;
    }

    public com.bear.generated.scheduledtransferworker.ScheduleStorePort workerScheduleStorePort() {
        return workerScheduleStorePort;
    }

    public void attachWorker(ScheduledTransferWorker worker) {
        this.scheduledTransferWorker = worker;
    }

    public BigDecimal getBalanceValue(String accountId) {
        return balances.getOrDefault(accountId, BigDecimal.ZERO);
    }

    public String getScheduleStatus(String scheduleId) {
        ScheduleRecord record = schedules.get(scheduleId);
        return record == null ? "" : record.status;
    }

    public int getScheduleAttempts(String scheduleId) {
        ScheduleRecord record = schedules.get(scheduleId);
        return record == null ? 0 : record.attempts;
    }

    public List<Map<String, String>> auditEvents() {
        return new ArrayList<>(auditEvents);
    }

    public void awaitAsyncWork() {
        try {
            Future<?> marker = workerExecutor.submit(() -> {
            });
            marker.get();
        } catch (Exception ex) {
            throw new IllegalStateException("failed waiting for async work", ex);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private static String dailyUsageKey(String accountId, String day) {
        return safe(accountId) + "|" + safe(day);
    }

    private static final class ScheduleRecord {
        private final String scheduleId;
        private final String sourceAccountId;
        private final String targetAccountId;
        private final BigDecimal amount;
        private final String scheduleDate;
        private volatile String status = "PENDING";
        private volatile int attempts;
        private volatile String failureReason = "";

        private ScheduleRecord(String scheduleId, String sourceAccountId, String targetAccountId, BigDecimal amount, String scheduleDate) {
            this.scheduleId = scheduleId;
            this.sourceAccountId = sourceAccountId;
            this.targetAccountId = targetAccountId;
            this.amount = amount;
            this.scheduleDate = scheduleDate;
        }
    }
}
