package styx.data;

import java.util.Objects;

public class Pair {
    private final Value key;
    private final Value value;

    public Pair(Value key, Value value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public Value key() {
        return key;
    }

    public Value value() {
        return value;
    }
}
