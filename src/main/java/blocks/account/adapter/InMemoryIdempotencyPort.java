package blocks.account.adapter;

import java.util.Map;

import blocks._shared.state.BankMemoryState;
import com.bear.generated.account.BearValue;
import com.bear.generated.account.IdempotencyPort;

public final class InMemoryIdempotencyPort implements IdempotencyPort {
    private final BankMemoryState state;

    public InMemoryIdempotencyPort(BankMemoryState state) {
        this.state = state;
    }

    @Override
    public BearValue get(BearValue input) {
        Map<String, String> payload = state.getIdempotencyValue(input.get("key"));
        if (payload == null) {
            return BearValue.empty();
        }
        BearValue.Builder builder = BearValue.builder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    @Override
    public BearValue put(BearValue input) {
        state.putIdempotencyValue(input.get("key"), input.asMap());
        return input;
    }
}