package styx.data;

/**
 * An immutable sequence of unicode characters.
 */
public interface Text extends Value {

    /**
     * Returns the number of characters of this value.
     *
     * @return the number of unicode characters.
     */
    public default int charCount() {
        return toCharString().length();
    }

    /**
     * Returns the character at the given index.
     *
     * @param index the index, must be in the range 0 .. charCount() - 1.
     * @return the unicode character at the given index.
     * @throws IndexOutOfBoundsException if the given index is invalid.
     */
    public default char charAt(int index) {
        return toCharString().charAt(index);
    }

    /**
     * Converts the value to a string.
     *
     * @return a string that contains the same unicode characters, never null.
     */
    public String toCharString();

    /**
     * Converts the value to a character array.
     *
     * @return a newly allocated character array object that contains the same unicode characters, never null.
     */
    public default char[] toCharArray() {
        return toCharString().toCharArray();
    }
}
