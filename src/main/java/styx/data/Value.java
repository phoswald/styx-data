package styx.data;

/**
 * An immutable value.
 */
public interface Value extends Comparable<Value> {

    public default Numeric asNumeric() {
        throw new ClassCastException();
    }

    public default Text asText() {
        throw new ClassCastException();
    }

    public default Binary asBinary() {
        throw new ClassCastException();
    }

    public default Reference asReference() {
        throw new ClassCastException();
    }
}
