package styx.data;

import java.util.Collection;
import java.util.Map;

import styx.data.impl.AbstractNumeric;
import styx.data.impl.BinaryTreeComplex;
import styx.data.impl.ByteArrayBinary;
import styx.data.impl.DefaultPair;
import styx.data.impl.LinkedListReference;
import styx.data.impl.Parser;
import styx.data.impl.StringText;

public class Values {

    public static Numeric number(long value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Numeric number(double value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Text text() {
        return text(null);
    }

    public static Text text(String value) {
        return StringText.valueOf(value);
    }

    public static Binary binary() {
        return binary(null);
    }

    public static Binary binary(byte[] value) {
        return ByteArrayBinary.valueOf(value);
    }

    public static Reference root() {
        return LinkedListReference.ROOT;
    }

    public static Reference reference(Value... parts) {
        return root().descendant(parts);
    }

    public static Reference reference(Collection<? extends Value> parts) {
        return root().descendant(parts);
    }

    public static Complex empty() {
        return BinaryTreeComplex.EMPTY;
    }

    public static Complex complex(Value key, Value value) {
        return empty().put(key, value);
    }

    public static Complex complex(Pair... pairs) {
        return empty().putAll(pairs);
    }

    public static Complex complex(Collection<Pair> pairs) {
        return empty().putAll(pairs);
    }

    public static Complex complex(Map<? extends Value, ? extends Value> values) {
        return empty().putAll(values);
    }

    public static Complex list(Value... values) {
        return empty().addAll(values);
    }

    public static Complex list(Collection<? extends Value> values) {
        return empty().addAll(values);
    }

    public static Pair pair(Value key, Value value) {
        return new DefaultPair(key, value);
    }

    public static Value parse(String input) {
        return new Parser(input).parse();
    }
}
