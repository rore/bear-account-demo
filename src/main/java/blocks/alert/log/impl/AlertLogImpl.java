package blocks.alert.log.impl;

import com.bear.generated.alert.log.AlertLogLogic;
import com.bear.generated.alert.log.AlertLog_AppendAlertRequest;
import com.bear.generated.alert.log.AlertLog_AppendAlertResult;
import com.bear.generated.alert.log.AlertLog_GetAlertsRequest;
import com.bear.generated.alert.log.AlertLog_GetAlertsResult;
import com.bear.generated.alert.log.AlertStorePort;
import com.bear.generated.alert.log.BearValue;

public final class AlertLogImpl implements AlertLogLogic {
    @Override
    public AlertLog_AppendAlertResult executeAppendAlert(AlertLog_AppendAlertRequest request, AlertStorePort alertStorePort) {
        requireNonBlank(request.getAccountId(), "accountId");
        requireNonBlank(request.getAlertType(), "alertType");
        requireNonBlank(request.getRequestId(), "requestId");
        requireNonBlank(request.getMessage(), "message");

        BearValue stored = alertStorePort.append(BearValue.builder()
            .put("accountId", request.getAccountId())
            .put("alertType", request.getAlertType())
            .put("requestId", request.getRequestId())
            .put("message", request.getMessage())
            .build());
        return new AlertLog_AppendAlertResult(parseInt(stored.get("alertSeq"), "alertSeq"));
    }

    @Override
    public AlertLog_GetAlertsResult executeGetAlerts(AlertLog_GetAlertsRequest request, AlertStorePort alertStorePort) {
        requireNonBlank(request.getAccountId(), "accountId");
        BearValue listed = alertStorePort.list(BearValue.builder()
            .put("accountId", request.getAccountId())
            .build());
        return new AlertLog_GetAlertsResult(defaultJson(listed.get("alertsJson")));
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static int parseInt(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(field + " is missing");
        }
        return Integer.parseInt(raw);
    }

    private static String defaultJson(String json) {
        return json == null ? "[]" : json;
    }
}
