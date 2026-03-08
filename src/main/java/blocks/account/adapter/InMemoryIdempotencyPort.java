package blocks.account.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

import com.bear.generated.account.BearValue;
import com.bear.generated.account.IdempotencyPort;

public final class InMemoryIdempotencyPort implements IdempotencyPort {
    private final Map<String, Map<String, String>> entries = new LinkedHashMap<>();

    @Override
    public BearValue get(BearValue input) {
        String key = required(input, "key");
        Map<String, String> stored = entries.get(key);
        if (stored == null) {
            return BearValue.empty();
        }
        BearValue.Builder builder = BearValue.builder();
        for (Map.Entry<String, String> entry : stored.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    @Override
    public BearValue put(BearValue input) {
        String key = required(input, "key");
        entries.put(key, new LinkedHashMap<>(input.asMap()));
        return input;
    }

    private static String required(BearValue input, String field) {
        String value = input == null ? null : input.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}