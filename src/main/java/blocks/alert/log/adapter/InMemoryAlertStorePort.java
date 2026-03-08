package blocks.alert.log.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bear.generated.alert.log.AlertStorePort;
import com.bear.generated.alert.log.BearValue;

public final class InMemoryAlertStorePort implements AlertStorePort {
    private final Map<String, List<AlertRecord>> alertsByAccount = new LinkedHashMap<>();

    @Override
    public BearValue append(BearValue input) {
        String accountId = required(input, "accountId");
        List<AlertRecord> alerts = alertsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
        int nextSeq = alerts.size() + 1;
        alerts.add(new AlertRecord(
            nextSeq,
            accountId,
            required(input, "alertType"),
            required(input, "requestId"),
            required(input, "message")));
        return BearValue.builder().put("alertSeq", Integer.toString(nextSeq)).build();
    }

    @Override
    public BearValue list(BearValue input) {
        String accountId = required(input, "accountId");
        List<AlertRecord> alerts = alertsByAccount.getOrDefault(accountId, List.of());
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (AlertRecord alert : alerts) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('{')
                .append("\"alertSeq\":").append(alert.alertSeq())
                .append(",\"accountId\":\"").append(escape(alert.accountId())).append('\"')
                .append(",\"alertType\":\"").append(escape(alert.alertType())).append('\"')
                .append(",\"requestId\":\"").append(escape(alert.requestId())).append('\"')
                .append(",\"message\":\"").append(escape(alert.message())).append('\"')
                .append('}');
        }
        json.append(']');
        return BearValue.builder().put("alertsJson", json.toString()).build();
    }

    private static String required(BearValue input, String field) {
        String value = input == null ? null : input.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record AlertRecord(int alertSeq, String accountId, String alertType, String requestId, String message) {
    }
}
