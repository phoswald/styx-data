package styx.data.impl;

class DoubleNumeric extends AbstractNumeric {

    private final double value;

    DoubleNumeric(double value) {
        this.value = value;
    }

    @Override
    public double toDouble() {
        return value;
    }
}
