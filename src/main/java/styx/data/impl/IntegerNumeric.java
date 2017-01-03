package styx.data.impl;

class IntegerNumeric extends AbstractNumeric {

    private final int value;

    IntegerNumeric(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int toInteger() {
        return value;
    }

    @Override
    public long toLong() {
        return value;
    }

    @Override
    public double toDouble() {
        return value;
    }
}
