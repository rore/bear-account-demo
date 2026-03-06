package com.bear.account.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
    private App.ServerHandle server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
    }

    @Test
    void accountFlowSupportsIdempotencyAndTransactionReads() throws Exception {
        server = App.start(0);
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> create = post(client, "/accounts", "{\"ownerId\":\"owner-1\"}");
        assertEquals(200, create.statusCode());
        String accountId = jsonString(create.body(), "accountId");

        HttpResponse<String> deposit = post(client, "/accounts/" + accountId + "/deposit", "{\"amountCents\":1500,\"requestId\":\"req-1\"}");
        assertEquals(200, deposit.statusCode());
        assertTrue(deposit.body().contains("\"balanceCents\":1500"));
        assertTrue(deposit.body().contains("\"txSeq\":1"));

        HttpResponse<String> replay = post(client, "/accounts/" + accountId + "/deposit", "{\"amountCents\":1500,\"requestId\":\"req-1\"}");
        assertEquals(200, replay.statusCode());
        assertEquals(deposit.body(), replay.body());

        HttpResponse<String> withdraw = post(client, "/accounts/" + accountId + "/withdraw", "{\"amountCents\":200,\"requestId\":\"req-2\"}");
        assertEquals(200, withdraw.statusCode());
        assertTrue(withdraw.body().contains("\"balanceCents\":1300"));
        assertTrue(withdraw.body().contains("\"txSeq\":2"));

        HttpResponse<String> balance = get(client, "/accounts/" + accountId + "/balance");
        assertEquals(200, balance.statusCode());
        assertEquals("{\"balanceCents\":1300}", balance.body());

        HttpResponse<String> transactions = get(client, "/accounts/" + accountId + "/transactions");
        assertEquals(200, transactions.statusCode());
        assertTrue(transactions.body().contains("\"seq\":1"));
        assertTrue(transactions.body().contains("\"type\":\"DEPOSIT\""));
        assertTrue(transactions.body().contains("\"seq\":2"));
        assertTrue(transactions.body().contains("\"type\":\"WITHDRAW\""));

        HttpResponse<String> sinceSeq = get(client, "/accounts/" + accountId + "/transactions?sinceSeq=1");
        assertEquals(200, sinceSeq.statusCode());
        assertTrue(sinceSeq.body().contains("\"seq\":2"));
        assertTrue(!sinceSeq.body().contains("\"seq\":1"));
    }

    @Test
    void withdrawRejectsInsufficientFundsAndBadInput() throws Exception {
        server = App.start(0);
        HttpClient client = HttpClient.newHttpClient();

        String accountId = jsonString(post(client, "/accounts", "{\"ownerId\":\"owner-2\"}").body(), "accountId");

        HttpResponse<String> insufficient = post(client, "/accounts/" + accountId + "/withdraw", "{\"amountCents\":1,\"requestId\":\"req-3\"}");
        assertEquals(409, insufficient.statusCode());

        HttpResponse<String> badRequest = post(client, "/accounts/" + accountId + "/deposit", "{\"amountCents\":0,\"requestId\":\"req-4\"}");
        assertEquals(400, badRequest.statusCode());

        HttpResponse<String> missing = get(client, "/accounts/missing/balance");
        assertEquals(404, missing.statusCode());
    }

    private HttpResponse<String> post(HttpClient client, String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri(path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(HttpClient client, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri(path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI baseUri(String path) {
        return URI.create("http://127.0.0.1:" + server.port() + path);
    }

    private String jsonString(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        int valueStart = start + marker.length();
        int end = json.indexOf('"', valueStart);
        return json.substring(valueStart, end);
    }
}