package styx.data.impl;

import styx.data.Kind;
import styx.data.Numeric;
import styx.data.Value;

public abstract class AbstractNumeric extends AbstractValue implements Numeric {

    public static Numeric valueOf(long value) {
        if(value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            return new IntegerNumeric((int) value);
        } else {
            return new LongNumeric(value);
        }
    }

    public static Numeric valueOf(double value) {
        long longValue = (long) value; // perform a lossy conversion
        if(longValue == value) {
            return valueOf(longValue); // the conversion was lossless
        } else {
            return new DoubleNumeric(value);
        }
    }

    @Override
    public int compareTo(Value other) {
        if(other.isNumeric()) {
            return Double.compare(toDouble(), other.asNumeric().toDouble());
        } else {
            return compare(kind(), other.kind());
        }
    }

    @Override
    public Kind kind() {
        return Kind.NUMBER;
    }

    @Override
    public int toInteger() {
        throw new ArithmeticException("The number cannot be represented exactly by an int.");
    }

    @Override
    public long toLong() {
        throw new ArithmeticException("The number cannot be represented exactly by a long.");
    }

    @Override
    public double toDouble() {
        throw new ArithmeticException("The number cannot be represented exactly by a double.");
    }
}
