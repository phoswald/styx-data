package styx.data.impl.value;

class IntegerNumeric extends AbstractNumeric {

    private final int value;

    IntegerNumeric(int value) {
        this.value = value;
    }

    @Override
    public boolean isInteger() {
        return true;
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

    @Override
    public String toDecimalString() {
        return Integer.toString(value);
    }
}
