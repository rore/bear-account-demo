package com.bear.account.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class AppTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void accountLifecycleAndTransactionFiltering() throws Exception {
        server = App.createServer(0);
        server.start();
        HttpClient client = HttpClient.newHttpClient();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        HttpResponse<String> create = send(client, "POST", baseUrl + "/accounts", "{\"ownerId\":\"owner-1\"}");
        assertEquals(200, create.statusCode());
        String accountId = extractString(create.body(), "accountId");

        HttpResponse<String> deposit = send(client, "POST", baseUrl + "/accounts/" + accountId + "/deposit", "{\"amountCents\":500,\"requestId\":\"req-1\"}");
        assertEquals(200, deposit.statusCode());
        assertEquals(500, extractInt(deposit.body(), "balanceCents"));
        assertEquals(1, extractInt(deposit.body(), "txSeq"));

        HttpResponse<String> withdraw = send(client, "POST", baseUrl + "/accounts/" + accountId + "/withdraw", "{\"amountCents\":125,\"requestId\":\"req-2\"}");
        assertEquals(200, withdraw.statusCode());
        assertEquals(375, extractInt(withdraw.body(), "balanceCents"));
        assertEquals(2, extractInt(withdraw.body(), "txSeq"));

        HttpResponse<String> balance = send(client, "GET", baseUrl + "/accounts/" + accountId + "/balance", null);
        assertEquals(200, balance.statusCode());
        assertEquals(375, extractInt(balance.body(), "balanceCents"));

        HttpResponse<String> transactions = send(client, "GET", baseUrl + "/accounts/" + accountId + "/transactions?sinceSeq=1", null);
        assertEquals(200, transactions.statusCode());
        assertEquals(1, countOccurrences(transactions.body(), "\"seq\":"));
        assertEquals(2, extractInt(transactions.body(), "seq"));
        assertEquals("WITHDRAW", extractString(transactions.body(), "type"));
    }

    @Test
    void successfulOperationsAreIdempotentButFailuresAreNotSticky() throws Exception {
        server = App.createServer(0);
        server.start();
        HttpClient client = HttpClient.newHttpClient();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        String accountId = extractString(send(client, "POST", baseUrl + "/accounts", "{\"ownerId\":\"owner-2\"}").body(), "accountId");

        HttpResponse<String> firstDeposit = send(client, "POST", baseUrl + "/accounts/" + accountId + "/deposit", "{\"amountCents\":300,\"requestId\":\"same-deposit\"}");
        HttpResponse<String> replayDeposit = send(client, "POST", baseUrl + "/accounts/" + accountId + "/deposit", "{\"amountCents\":300,\"requestId\":\"same-deposit\"}");
        assertEquals(200, firstDeposit.statusCode());
        assertEquals(firstDeposit.body(), replayDeposit.body());

        HttpResponse<String> failedWithdraw = send(client, "POST", baseUrl + "/accounts/" + accountId + "/withdraw", "{\"amountCents\":500,\"requestId\":\"same-withdraw\"}");
        assertEquals(409, failedWithdraw.statusCode());

        HttpResponse<String> secondDeposit = send(client, "POST", baseUrl + "/accounts/" + accountId + "/deposit", "{\"amountCents\":400,\"requestId\":\"top-up\"}");
        assertEquals(200, secondDeposit.statusCode());

        HttpResponse<String> retriedWithdraw = send(client, "POST", baseUrl + "/accounts/" + accountId + "/withdraw", "{\"amountCents\":500,\"requestId\":\"same-withdraw\"}");
        assertEquals(200, retriedWithdraw.statusCode());
        assertEquals(200, extractInt(retriedWithdraw.body(), "balanceCents"));
        assertEquals(3, extractInt(retriedWithdraw.body(), "txSeq"));
    }

    @Test
    void validationAndNotFoundErrorsMatchSpec() throws Exception {
        server = App.createServer(0);
        server.start();
        HttpClient client = HttpClient.newHttpClient();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        HttpResponse<String> missingRequestId = send(client, "POST", baseUrl + "/accounts/missing/deposit", "{\"amountCents\":100}");
        assertEquals(400, missingRequestId.statusCode());

        HttpResponse<String> missingAccount = send(client, "GET", baseUrl + "/accounts/missing/balance", null);
        assertEquals(404, missingAccount.statusCode());

        String accountId = extractString(send(client, "POST", baseUrl + "/accounts", "{\"ownerId\":\"owner-3\"}").body(), "accountId");
        HttpResponse<String> invalidSince = send(client, "GET", baseUrl + "/accounts/" + accountId + "/transactions?sinceSeq=-1", null);
        assertEquals(400, invalidSince.statusCode());
    }

    private static HttpResponse<String> send(HttpClient client, String method, String url, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String extractString(String json, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("missing field: " + field + " in " + json);
        }
        return matcher.group(1);
    }

    private static int extractInt(String json, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("missing field: " + field + " in " + json);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}