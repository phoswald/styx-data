package styx.data.impl.value;

class DoubleNumeric extends AbstractNumeric {

    private final double value;

    DoubleNumeric(double value) {
        this.value = value;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public String toDecimalString() {
        return Double.toString(value);
    }
}
