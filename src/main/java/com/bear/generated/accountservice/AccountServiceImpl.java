package com.bear.generated.accountservice;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class AccountServiceImpl implements AccountServiceLogic {
    private static final String OP_DEPOSIT = "DEPOSIT";
    private static final String OP_CREATE_SCHEDULE = "CREATE_SCHEDULE";

    @Override
    public AccountServiceResult execute(AccountServiceRequest request, AccountStorePort accountStorePort, AuditPort auditPort, IdempotencyPort idempotencyPort, ScheduleStorePort scheduleStorePort, WorkQueuePort workQueuePort) {
        String operation = safe(request.getOperation());
        if (OP_DEPOSIT.equals(operation)) {
            return handleDeposit(request, accountStorePort);
        }
        if (OP_CREATE_SCHEDULE.equals(operation)) {
            return handleCreateSchedule(request, accountStorePort, auditPort, scheduleStorePort, workQueuePort);
        }
        BigDecimal currentBalance = readBalance(accountStorePort, request.getAccountId());
        return new AccountServiceResult(Boolean.FALSE, currentBalance, "UNSUPPORTED_OPERATION", "");
    }

    private AccountServiceResult handleDeposit(AccountServiceRequest request, AccountStorePort accountStorePort) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new AccountServiceResult(Boolean.FALSE, readBalance(accountStorePort, request.getAccountId()), "INVALID_AMOUNT", "");
        }
        BigDecimal current = readBalance(accountStorePort, request.getAccountId());
        BigDecimal updated = current.add(request.getAmount());
        accountStorePort.putBalance(BearValue.builder()
            .put("accountId", safe(request.getAccountId()))
            .put("balance", updated.toPlainString())
            .build());
        return new AccountServiceResult(Boolean.TRUE, updated, "DEPOSIT_APPLIED", "");
    }

    private AccountServiceResult handleCreateSchedule(
        AccountServiceRequest request,
        AccountStorePort accountStorePort,
        AuditPort auditPort,
        ScheduleStorePort scheduleStorePort,
        WorkQueuePort workQueuePort
    ) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new AccountServiceResult(Boolean.FALSE, readBalance(accountStorePort, request.getAccountId()), "INVALID_AMOUNT", "");
        }
        if (blank(request.getDestinationAccountId())) {
            return new AccountServiceResult(Boolean.FALSE, readBalance(accountStorePort, request.getAccountId()), "DESTINATION_REQUIRED", "");
        }
        LocalDate scheduleDate;
        try {
            scheduleDate = LocalDate.parse(safe(request.getScheduleDate()));
        } catch (RuntimeException ex) {
            return new AccountServiceResult(Boolean.FALSE, readBalance(accountStorePort, request.getAccountId()), "INVALID_SCHEDULE_DATE", "");
        }

        String scheduleId = "sch-" + safe(request.getRequestId());
        scheduleStorePort.put(BearValue.builder()
            .put("scheduleId", scheduleId)
            .put("sourceAccountId", safe(request.getAccountId()))
            .put("targetAccountId", safe(request.getDestinationAccountId()))
            .put("amount", request.getAmount().toPlainString())
            .put("scheduleDate", scheduleDate.toString())
            .build());
        workQueuePort.enqueue(BearValue.builder()
            .put("scheduleId", scheduleId)
            .build());
        auditPort.emit(BearValue.builder()
            .put("eventType", "SCHEDULE_CREATED")
            .put("scheduleId", scheduleId)
            .put("requestId", safe(request.getRequestId()))
            .put("sourceAccountId", safe(request.getAccountId()))
            .put("targetAccountId", safe(request.getDestinationAccountId()))
            .put("amount", request.getAmount().toPlainString())
            .put("scheduleDate", scheduleDate.toString())
            .build());
        return new AccountServiceResult(Boolean.TRUE, readBalance(accountStorePort, request.getAccountId()), "SCHEDULE_CREATED", scheduleId);
    }

    private static BigDecimal readBalance(AccountStorePort accountStorePort, String accountId) {
        BearValue found = accountStorePort.getBalance(BearValue.builder().put("accountId", safe(accountId)).build());
        if (found == null || found.get("balance") == null || found.get("balance").isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(found.get("balance"));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
