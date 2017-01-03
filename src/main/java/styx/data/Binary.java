package styx.data;

/**
 * An immutable sequence of bytes.
 */
public interface Binary extends Value {

    /**
     * Returns the number of bytes of this value.
     *
     * @return the number of bytes.
     */
    public int byteCount();

    /**
     * Returns the byte at the given index.
     *
     * @param index the index, must be in the range 0 .. byteCount() - 1.
     * @return the byte at the given index.
     * @throws IndexOutOfBoundsException if the given index is invalid.
     */
    public byte byteAt(int index);

    /**
     * Converts the value to a byte array.
     *
     * @return a newly allocated byte array that contains the same bytes, never null.
     */
    public byte[] toByteArray();
}
