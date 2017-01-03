package styx.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An immutable, ordered map of key/value-pairs.
 */
public interface Complex extends Value, Iterable<Pair> {

    public default Stream<Pair> entries() {
        return StreamSupport.stream(spliterator(), false);
    }

    public default Stream<Value> keys() {
        return entries().map(Pair::key);
    }

    public default Stream<Value> values() {
        return entries().map(Pair::value);
    }

    public default List<Pair> allEntries() {
        return entries().collect(Collectors.toList());
    }

    public default List<Value> allKeys() {
        return keys().collect(Collectors.toList());
    }

    public default List<Value> allValues() {
        return values().collect(Collectors.toList());
    }

    public Optional<Value> get(Value key);

    public Complex put(Value key, Value value);

    public default Complex putAll(Pair... pairs) {
        return putAll(pairs == null ? null : Arrays.asList(pairs));
    }

    public default Complex putAll(Collection<Pair> pairs) {
        Complex current = this;
        if(pairs != null) {
            for(Pair pair : pairs) {
                current = current.put(pair.key(), pair.value());
            }
        }
        return current;
    }

    public default Complex putAll(Map<? extends Value, ? extends Value> values) {
        Complex current = this;
        if(values != null) {
            for(Map.Entry<? extends Value, ? extends Value> entry : values.entrySet()) {
                current = current.put(entry.getKey(), entry.getValue());
            }
        }
        return current;
    }

//    public default Complex add(Value value) {
//        return put(Values.number(nextIndex()), value);
//    }

    public default Complex addAll(Value... values) {
        return addAll(values == null ? null : Arrays.asList(values));
    }

    public default Complex addAll(Collection<? extends Value> values) {
        Complex current = this;
        if(values != null && !values.isEmpty()) {
            long nextIndex = nextIndex();
            for(Value value : values) {
                current = current.put(Values.number(nextIndex++), value);
            }
        }
        return current;
    }

    public long nextIndex();
}
