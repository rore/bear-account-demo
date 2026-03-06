package com.bear.account.demo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import blocks._shared.state.BankMemoryState;
import blocks.account.adapter.InMemoryAccountStatePort;
import blocks.account.adapter.InMemoryIdempotencyPort;
import blocks.account.impl.AccountNotFoundException;
import blocks.account.impl.InsufficientFundsException;
import blocks.transaction.log.adapter.InMemoryTransactionStatePort;

import com.bear.generated.account.Account_CreateAccount;
import com.bear.generated.account.Account_CreateAccountRequest;
import com.bear.generated.account.Account_Deposit;
import com.bear.generated.account.Account_DepositRequest;
import com.bear.generated.account.Account_GetBalance;
import com.bear.generated.account.Account_GetBalanceRequest;
import com.bear.generated.account.Account_TransactionLogBlockClient;
import com.bear.generated.account.Account_Withdraw;
import com.bear.generated.account.Account_WithdrawRequest;
import com.bear.generated.transaction.log.TransactionLog_AppendTransaction;
import com.bear.generated.transaction.log.TransactionLog_GetTransactions;
import com.bear.generated.transaction.log.TransactionLog_GetTransactionsRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class App {
    public static void main(String[] args) throws IOException {
        ServerHandle handle = start(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(handle::close));
        System.out.println("Server listening on http://localhost:" + handle.port());
    }

    public static ServerHandle start(int port) throws IOException {
        BankApplication application = new BankApplication();
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(port), 0);
        server.createContext("/accounts", application::handle);
        server.start();
        return new ServerHandle(server);
    }

    public static final class ServerHandle implements AutoCloseable {
        private final HttpServer server;

        private ServerHandle(HttpServer server) {
            this.server = server;
        }

        public int port() {
            return server.getAddress().getPort();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}

final class BankApplication {
    private final Account_CreateAccount createAccount;
    private final Account_Deposit deposit;
    private final Account_Withdraw withdraw;
    private final Account_GetBalance getBalance;
    private final TransactionLog_GetTransactions getTransactions;

    BankApplication() {
        BankMemoryState state = new BankMemoryState();
        InMemoryAccountStatePort accountStatePort = new InMemoryAccountStatePort(state);
        InMemoryIdempotencyPort idempotencyPort = new InMemoryIdempotencyPort(state);
        InMemoryTransactionStatePort transactionStatePort = new InMemoryTransactionStatePort(state);
        TransactionLog_AppendTransaction appendTransaction = TransactionLog_AppendTransaction.of(transactionStatePort);
        Account_TransactionLogBlockClient transactionLogPort = new Account_TransactionLogBlockClient(appendTransaction);
        this.createAccount = Account_CreateAccount.of(accountStatePort, idempotencyPort, transactionLogPort);
        this.deposit = Account_Deposit.of(accountStatePort, idempotencyPort, transactionLogPort);
        this.withdraw = Account_Withdraw.of(accountStatePort, idempotencyPort, transactionLogPort);
        this.getBalance = Account_GetBalance.of(accountStatePort, idempotencyPort, transactionLogPort);
        this.getTransactions = TransactionLog_GetTransactions.of(transactionStatePort);
    }

    void handle(HttpExchange exchange) throws IOException {
        try {
            dispatch(exchange);
        } catch (IllegalArgumentException e) {
            writeJson(exchange, 400, errorJson(e.getMessage()));
        } catch (AccountNotFoundException e) {
            writeJson(exchange, 404, errorJson(e.getMessage()));
        } catch (InsufficientFundsException e) {
            writeJson(exchange, 409, errorJson(e.getMessage()));
        } catch (Exception e) {
            writeJson(exchange, 500, errorJson("internal server error"));
        } finally {
            exchange.close();
        }
    }

    private void dispatch(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/accounts".equals(path)) {
            handleCreateAccount(exchange);
            return;
        }

        if (parts.length < 4 || !"accounts".equals(parts[1])) {
            writeJson(exchange, 404, errorJson("not found"));
            return;
        }

        String accountId = parts[2];
        String action = parts[3];

        if ("POST".equals(method) && "deposit".equals(action)) {
            handleDeposit(exchange, accountId);
            return;
        }
        if ("POST".equals(method) && "withdraw".equals(action)) {
            handleWithdraw(exchange, accountId);
            return;
        }
        if ("GET".equals(method) && "balance".equals(action)) {
            handleGetBalance(exchange, accountId);
            return;
        }
        if ("GET".equals(method) && "transactions".equals(action)) {
            handleGetTransactions(exchange, accountId);
            return;
        }

        writeJson(exchange, 404, errorJson("not found"));
    }

    private void handleCreateAccount(HttpExchange exchange) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        String ownerId = requireString(body, "ownerId");
        String accountId = createAccount.execute(new Account_CreateAccountRequest(ownerId)).getAccountId();
        writeJson(exchange, 200, "{\"accountId\":\"" + escapeJson(accountId) + "\"}");
    }

    private void handleDeposit(HttpExchange exchange, String accountId) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        int amountCents = requireInt(body, "amountCents");
        String requestId = requireString(body, "requestId");
        var result = deposit.execute(new Account_DepositRequest(accountId, amountCents, requestId));
        writeJson(exchange, 200, "{\"balanceCents\":" + result.getBalanceCents() + ",\"txSeq\":" + result.getTxSeq() + "}");
    }

    private void handleWithdraw(HttpExchange exchange, String accountId) throws IOException {
        Map<String, String> body = parseJsonObject(readBody(exchange));
        int amountCents = requireInt(body, "amountCents");
        String requestId = requireString(body, "requestId");
        var result = withdraw.execute(new Account_WithdrawRequest(accountId, amountCents, requestId));
        writeJson(exchange, 200, "{\"balanceCents\":" + result.getBalanceCents() + ",\"txSeq\":" + result.getTxSeq() + "}");
    }

    private void handleGetBalance(HttpExchange exchange, String accountId) throws IOException {
        int balance = getBalance.execute(new Account_GetBalanceRequest(accountId)).getBalanceCents();
        writeJson(exchange, 200, "{\"balanceCents\":" + balance + "}");
    }

    private void handleGetTransactions(HttpExchange exchange, String accountId) throws IOException {
        getBalance.execute(new Account_GetBalanceRequest(accountId));
        int sinceSeq = parseSinceSeq(exchange.getRequestURI().getRawQuery());
        String payload = getTransactions.execute(new TransactionLog_GetTransactionsRequest(accountId, sinceSeq)).getTransactionsJson();
        writeJson(exchange, 200, payload);
    }

    private int parseSinceSeq(String query) {
        if (query == null || query.isBlank()) {
            return 0;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "sinceSeq".equals(parts[0])) {
                int parsed = Integer.parseInt(parts[1]);
                if (parsed < 0) {
                    throw new IllegalArgumentException("sinceSeq must be >= 0");
                }
                return parsed;
            }
        }
        return 0;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream stream = exchange.getRequestBody()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseJsonObject(String body) {
        String trimmed = body == null ? "" : body.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("invalid json object");
        }
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        Map<String, String> values = new HashMap<>();
        if (inner.isEmpty()) {
            return values;
        }
        String[] pairs = inner.split(",");
        for (String pair : pairs) {
            String[] entry = pair.split(":", 2);
            if (entry.length != 2) {
                throw new IllegalArgumentException("invalid json field");
            }
            String key = stripQuotes(entry[0].trim());
            String rawValue = entry[1].trim();
            if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                values.put(key, stripQuotes(rawValue));
            } else {
                values.put(key, rawValue);
            }
        }
        return values;
    }

    private String stripQuotes(String raw) {
        String trimmed = raw.trim();
        if (!trimmed.startsWith("\"") || !trimmed.endsWith("\"")) {
            throw new IllegalArgumentException("invalid json string");
        }
        return trimmed.substring(1, trimmed.length() - 1);
    }

    private String requireString(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private int requireInt(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be an int");
        }
    }

    private void writeJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private String errorJson(String message) {
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}