package styx.data;

/**
 * An immutable value.
 */
public interface Value extends Comparable<Value> {

    public default boolean isNumeric() {
        return this instanceof Numeric;
    }

    public default Numeric asNumeric() {
        return (Numeric) this;
    }

    public default boolean isText() {
        return this instanceof Text;
    }

    public default Text asText() {
        return (Text) this;
    }

    public default boolean isBinary() {
        return this instanceof Binary;
    }

    public default Binary asBinary() {
        return (Binary) this;
    }

    public default boolean isReference() {
        return this instanceof Reference;
    }

    public default Reference asReference() {
        return (Reference) this;
    }

    public default boolean isComplex() {
        return this instanceof Complex;
    }

    public default Complex asComplex() {
        return (Complex) this;
    }
}
