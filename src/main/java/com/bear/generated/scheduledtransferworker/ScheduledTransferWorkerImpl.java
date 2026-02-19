package com.bear.generated.scheduledtransferworker;

import java.math.BigDecimal;

public final class ScheduledTransferWorkerImpl implements ScheduledTransferWorkerLogic {
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("1000.00");

    @Override
    public ScheduledTransferWorkerResult execute(ScheduledTransferWorkerRequest request, AccountStorePort accountStorePort, AuditPort auditPort, DailyLimitStorePort dailyLimitStorePort, IdempotencyPort idempotencyPort, ScheduleStorePort scheduleStorePort) {
        BearValue schedule = scheduleStorePort.get(BearValue.builder().put("scheduleId", safe(request.getScheduleId())).build());
        if (schedule == null || blank(schedule.get("scheduleId"))) {
            auditPort.emit(BearValue.builder()
                .put("eventType", "SCHEDULE_EXECUTION_FAILED")
                .put("scheduleId", safe(request.getScheduleId()))
                .put("reason", "SCHEDULE_NOT_FOUND")
                .build());
            return new ScheduledTransferWorkerResult(Boolean.FALSE, "SCHEDULE_NOT_FOUND", BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String sourceAccountId = safe(schedule.get("sourceAccountId"));
        String targetAccountId = safe(schedule.get("targetAccountId"));
        String scheduleDate = safe(schedule.get("scheduleDate"));
        BigDecimal amount = parseAmount(schedule.get("amount"));
        BigDecimal sourceBalance = readBalance(accountStorePort, sourceAccountId);
        BigDecimal targetBalance = readBalance(accountStorePort, targetAccountId);

        BigDecimal usedToday = readUsed(dailyLimitStorePort, sourceAccountId, scheduleDate);
        if (usedToday.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            scheduleStorePort.markFailed(BearValue.builder()
                .put("scheduleId", safe(request.getScheduleId()))
                .put("reason", "DAILY_LIMIT_EXCEEDED")
                .build());
            auditPort.emit(BearValue.builder()
                .put("eventType", "SCHEDULE_EXECUTION_FAILED")
                .put("scheduleId", safe(request.getScheduleId()))
                .put("reason", "DAILY_LIMIT_EXCEEDED")
                .build());
            return new ScheduledTransferWorkerResult(Boolean.FALSE, "DAILY_LIMIT_EXCEEDED", sourceBalance, targetBalance);
        }

        if (sourceBalance.compareTo(amount) < 0) {
            scheduleStorePort.markFailed(BearValue.builder()
                .put("scheduleId", safe(request.getScheduleId()))
                .put("reason", "INSUFFICIENT_FUNDS")
                .build());
            auditPort.emit(BearValue.builder()
                .put("eventType", "SCHEDULE_EXECUTION_FAILED")
                .put("scheduleId", safe(request.getScheduleId()))
                .put("reason", "INSUFFICIENT_FUNDS")
                .build());
            return new ScheduledTransferWorkerResult(Boolean.FALSE, "INSUFFICIENT_FUNDS", sourceBalance, targetBalance);
        }

        BigDecimal updatedSource = sourceBalance.subtract(amount);
        BigDecimal updatedTarget = targetBalance.add(amount);
        accountStorePort.putBalance(BearValue.builder()
            .put("accountId", sourceAccountId)
            .put("balance", updatedSource.toPlainString())
            .build());
        accountStorePort.putBalance(BearValue.builder()
            .put("accountId", targetAccountId)
            .put("balance", updatedTarget.toPlainString())
            .build());
        dailyLimitStorePort.putUsed(BearValue.builder()
            .put("accountId", sourceAccountId)
            .put("day", scheduleDate)
            .put("used", usedToday.add(amount).toPlainString())
            .build());
        scheduleStorePort.markExecuted(BearValue.builder()
            .put("scheduleId", safe(request.getScheduleId()))
            .build());
        auditPort.emit(BearValue.builder()
            .put("eventType", "SCHEDULE_EXECUTION_SUCCEEDED")
            .put("scheduleId", safe(request.getScheduleId()))
            .put("sourceAccountId", sourceAccountId)
            .put("targetAccountId", targetAccountId)
            .put("amount", amount.toPlainString())
            .build());
        return new ScheduledTransferWorkerResult(Boolean.TRUE, "", updatedSource, updatedTarget);
    }

    private static BigDecimal readBalance(AccountStorePort accountStorePort, String accountId) {
        BearValue found = accountStorePort.getBalance(BearValue.builder().put("accountId", safe(accountId)).build());
        if (found == null || blank(found.get("balance"))) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(found.get("balance"));
    }

    private static BigDecimal readUsed(DailyLimitStorePort dailyLimitStorePort, String accountId, String day) {
        BearValue found = dailyLimitStorePort.getUsed(BearValue.builder().put("accountId", safe(accountId)).put("day", safe(day)).build());
        if (found == null || blank(found.get("used"))) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(found.get("used"));
    }

    private static BigDecimal parseAmount(String value) {
        if (blank(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
