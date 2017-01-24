package styx.data.impl.value;

import styx.data.Pair;
import styx.data.Value;

public class DefaultPair implements Pair {

    private final Value key;
    private final Value value;

    public DefaultPair(Value key, Value value) {
        if(key == null || value == null) {
            throw new IllegalArgumentException("The key and the value must not be null.");
        }
        this.key = key;
        this.value = value;
    }

    @Override
    public Value key() {
        return key;
    }

    @Override
    public Value value() {
        return value;
    }
}
