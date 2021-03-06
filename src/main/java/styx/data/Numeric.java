package styx.data;

/**
 * An immutable, arbitrary-precision signed decimal number.
 */
public interface Numeric extends Value {

    /**
     * Checks whether the number can be represented exactly by a long.
     *
     * @return true if toInteger() will succeed, fals if toInteger() will fail.
     */
    boolean isInteger();

    /**
     * Converts the number to a 32-bit signed integer value.
     *
     * @return an int that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by an int.
     */
    public int toInteger();

    /**
     * Converts the number to a 64-bit signed integer value.
     *
     * @return a long that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by a long.
     */
    public long toLong();

    /**
     * Converts the number to a 64-bit floating point value.
     *
     * @return a double that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by a double.
     */
    public double toDouble();

    /**
     * Converts the number to a decimal string representation.
     *
     * @return the exact decimal string representation.
     */
    public String toDecimalString();
}
