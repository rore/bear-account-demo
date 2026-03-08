package com.bear.account.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import blocks.account.impl.AccountNotFoundException;
import blocks.account.impl.InsufficientFundsException;

public final class App {
    private final AccountApplication application;

    public App(AccountApplication application) {
        this.application = application;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = createServer(8080);
        server.start();
    }

    public static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(port), 0);
        App app = new App(new AccountApplication());
        server.createContext("/", app::handle);
        return server;
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");

            if ("POST".equals(method) && "/accounts".equals(path)) {
                handleCreateAccount(exchange);
                return;
            }
            if (segments.length == 4 && "accounts".equals(segments[1]) && "balance".equals(segments[3]) && "GET".equals(method)) {
                handleGetBalance(exchange, segments[2]);
                return;
            }
            if (segments.length == 4 && "accounts".equals(segments[1]) && "deposit".equals(segments[3]) && "POST".equals(method)) {
                handleDeposit(exchange, segments[2]);
                return;
            }
            if (segments.length == 4 && "accounts".equals(segments[1]) && "withdraw".equals(segments[3]) && "POST".equals(method)) {
                handleWithdraw(exchange, segments[2]);
                return;
            }
            if (segments.length == 4 && "accounts".equals(segments[1]) && "transactions".equals(segments[3]) && "GET".equals(method)) {
                handleGetTransactions(exchange, segments[2]);
                return;
            }
            if (segments.length == 4 && "accounts".equals(segments[1]) && "alerts".equals(segments[3]) && "GET".equals(method)) {
                handleGetAlerts(exchange, segments[2]);
                return;
            }

            sendJson(exchange, 404, "{\"error\":\"not found\"}");
        } catch (AccountNotFoundException e) {
            sendJson(exchange, 404, "{\"error\":\"account not found\"}");
        } catch (InsufficientFundsException e) {
            sendJson(exchange, 409, "{\"error\":\"insufficient funds\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"internal server error\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleCreateAccount(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String ownerId = requireString(body, "ownerId");
        String accountId = application.createAccount(ownerId);
        sendJson(exchange, 200, "{\"accountId\":\"" + escape(accountId) + "\"}");
    }

    private void handleDeposit(HttpExchange exchange, String accountId) throws IOException {
        String body = readBody(exchange);
        AccountApplication.OperationResult result = application.deposit(accountId, requireInt(body, "amountCents"), requireString(body, "requestId"));
        sendJson(exchange, 200, "{\"balanceCents\":" + result.balanceCents() + ",\"txSeq\":" + result.txSeq() + "}");
    }

    private void handleWithdraw(HttpExchange exchange, String accountId) throws IOException {
        String body = readBody(exchange);
        AccountApplication.OperationResult result = application.withdraw(accountId, requireInt(body, "amountCents"), requireString(body, "requestId"));
        sendJson(exchange, 200, "{\"balanceCents\":" + result.balanceCents() + ",\"txSeq\":" + result.txSeq() + "}");
    }

    private void handleGetBalance(HttpExchange exchange, String accountId) throws IOException {
        int balanceCents = application.getBalance(accountId);
        sendJson(exchange, 200, "{\"balanceCents\":" + balanceCents + "}");
    }

    private void handleGetTransactions(HttpExchange exchange, String accountId) throws IOException {
        int sinceSeq = readSinceSeq(exchange.getRequestURI().getRawQuery());
        String transactionsJson = application.getTransactionsJson(accountId, sinceSeq);
        sendJson(exchange, 200, "{\"transactions\":" + transactionsJson + "}");
    }

    private void handleGetAlerts(HttpExchange exchange, String accountId) throws IOException {
        String alertsJson = application.getAlertsJson(accountId);
        sendJson(exchange, 200, "{\"alerts\":" + alertsJson + "}");
    }

    private static int readSinceSeq(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return 0;
        }
        for (String part : rawQuery.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && "sinceSeq".equals(pair[0])) {
                int value = Integer.parseInt(pair[1]);
                if (value < 0) {
                    throw new IllegalArgumentException("sinceSeq must be >= 0");
                }
                return value;
            }
        }
        return 0;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
    }

    private static String requireString(String body, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
        if (!matcher.find() || matcher.group(1).isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return matcher.group(1);
    }

    private static Integer requireInt(String body, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)").matcher(body);
        if (!matcher.find()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return Integer.valueOf(matcher.group(1));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
