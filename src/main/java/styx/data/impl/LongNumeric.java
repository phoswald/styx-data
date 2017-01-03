package styx.data.impl;

class LongNumeric extends AbstractNumeric {

    private final long value;

    LongNumeric(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
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
