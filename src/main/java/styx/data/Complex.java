package styx.data;

import java.util.Optional;

/**
 * An immutable, ordered map of key/value-pairs.
 */
public interface Complex extends Value {

    public Optional<Value> get(Value key);

    public Complex put(Value key, Value value);
}
