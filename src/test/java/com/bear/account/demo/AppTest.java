package com.bear.account.demo;

import com.bear.generated.accountservice.AccountService;
import com.bear.generated.accountservice.AccountServiceImpl;
import com.bear.generated.accountservice.AccountServiceRequest;
import com.bear.generated.accountservice.AccountServiceResult;
import com.bear.generated.scheduledtransferworker.ScheduledTransferWorker;
import com.bear.generated.scheduledtransferworker.ScheduledTransferWorkerImpl;
import com.bear.generated.scheduledtransferworker.ScheduledTransferWorkerRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
    @Test
    void depositIsImmediateAndIdempotent() {
        Fixture fixture = fixture();

        AccountServiceRequest request = new AccountServiceRequest(
            "acct-1",
            new BigDecimal("100.00"),
            "",
            "DEPOSIT",
            "req-dep-1",
            ""
        );

        AccountServiceResult first = fixture.accountService.execute(request);
        AccountServiceResult replay = fixture.accountService.execute(request);

        assertTrue(first.getAccepted());
        assertEquals(new BigDecimal("100.00"), first.getBalance());
        assertEquals(new BigDecimal("100.00"), replay.getBalance());
        assertEquals(new BigDecimal("100.00"), fixture.ports.getBalanceValue("acct-1"));
    }

    @Test
    void scheduledTransferExecutesAsynchronouslyAndEmitsCreateAndSuccessEvents() {
        Fixture fixture = fixture();
        fixture.accountService.execute(new AccountServiceRequest("source", new BigDecimal("600.00"), "", "DEPOSIT", "req-dep-2", ""));

        AccountServiceResult scheduled = fixture.accountService.execute(new AccountServiceRequest(
            "source",
            new BigDecimal("200.00"),
            "target",
            "CREATE_SCHEDULE",
            "req-sched-1",
            "2026-02-19"
        ));

        fixture.ports.awaitAsyncWork();

        assertTrue(scheduled.getAccepted());
        assertEquals(new BigDecimal("400.00"), fixture.ports.getBalanceValue("source"));
        assertEquals(new BigDecimal("200.00"), fixture.ports.getBalanceValue("target"));
        assertEquals("EXECUTED", fixture.ports.getScheduleStatus(scheduled.getScheduleId()));
        assertEquals(1, fixture.ports.getScheduleAttempts(scheduled.getScheduleId()));

        List<Map<String, String>> events = fixture.ports.auditEvents();
        assertTrue(events.stream().anyMatch(event -> "SCHEDULE_CREATED".equals(event.get("eventType"))));
        assertTrue(events.stream().anyMatch(event -> "SCHEDULE_EXECUTION_SUCCEEDED".equals(event.get("eventType"))));
    }

    @Test
    void scheduledTransferFailureUsesNoRetryAndEmitsFailureEvent() {
        Fixture fixture = fixture();
        fixture.accountService.execute(new AccountServiceRequest("limited", new BigDecimal("2000.00"), "", "DEPOSIT", "req-dep-3", ""));

        AccountServiceResult first = fixture.accountService.execute(new AccountServiceRequest(
            "limited",
            new BigDecimal("800.00"),
            "sink",
            "CREATE_SCHEDULE",
            "req-sched-2",
            "2026-02-19"
        ));
        fixture.ports.awaitAsyncWork();

        AccountServiceResult second = fixture.accountService.execute(new AccountServiceRequest(
            "limited",
            new BigDecimal("300.00"),
            "sink",
            "CREATE_SCHEDULE",
            "req-sched-3",
            "2026-02-19"
        ));
        fixture.ports.awaitAsyncWork();

        assertEquals("EXECUTED", fixture.ports.getScheduleStatus(first.getScheduleId()));
        assertEquals("FAILED", fixture.ports.getScheduleStatus(second.getScheduleId()));
        assertEquals(1, fixture.ports.getScheduleAttempts(second.getScheduleId()));
        assertEquals(new BigDecimal("1200.00"), fixture.ports.getBalanceValue("limited"));
        assertEquals(new BigDecimal("800.00"), fixture.ports.getBalanceValue("sink"));

        List<Map<String, String>> events = fixture.ports.auditEvents();
        assertTrue(events.stream().anyMatch(event -> "SCHEDULE_EXECUTION_FAILED".equals(event.get("eventType")) && "DAILY_LIMIT_EXCEEDED".equals(event.get("reason"))));
    }

    @Test
    void workerPathIsIdempotentForSameWorkerRequestId() {
        Fixture fixture = fixture();
        fixture.accountService.execute(new AccountServiceRequest("src", new BigDecimal("300.00"), "", "DEPOSIT", "req-dep-4", ""));

        AccountServiceResult created = fixture.accountService.execute(new AccountServiceRequest(
            "src",
            new BigDecimal("50.00"),
            "dst",
            "CREATE_SCHEDULE",
            "req-sched-4",
            "2026-02-19"
        ));
        fixture.ports.awaitAsyncWork();

        BigDecimal sourceAfterFirstExecution = fixture.ports.getBalanceValue("src");
        BigDecimal targetAfterFirstExecution = fixture.ports.getBalanceValue("dst");

        fixture.worker.execute(new ScheduledTransferWorkerRequest(created.getScheduleId(), "worker:" + created.getScheduleId()));

        assertEquals(sourceAfterFirstExecution, fixture.ports.getBalanceValue("src"));
        assertEquals(targetAfterFirstExecution, fixture.ports.getBalanceValue("dst"));
    }

    private static Fixture fixture() {
        SharedPorts ports = new SharedPorts();
        ScheduledTransferWorker worker = new ScheduledTransferWorker(
            ports.workerAccountStorePort(),
            ports.workerAuditPort(),
            ports.workerDailyLimitStorePort(),
            ports.workerIdempotencyPort(),
            ports.workerScheduleStorePort(),
            new ScheduledTransferWorkerImpl()
        );
        ports.attachWorker(worker);
        AccountService accountService = new AccountService(
            ports.accountServiceAccountStorePort(),
            ports.accountServiceAuditPort(),
            ports.accountServiceIdempotencyPort(),
            ports.accountServiceScheduleStorePort(),
            ports.accountServiceWorkQueuePort(),
            new AccountServiceImpl()
        );
        return new Fixture(accountService, worker, ports);
    }

    private record Fixture(AccountService accountService, ScheduledTransferWorker worker, SharedPorts ports) {
    }
}
