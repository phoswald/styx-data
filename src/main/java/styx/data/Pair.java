package styx.data;

/**
 * An immutable key/value pair.
 */
public interface Pair {

    /**
     * Returns the key.
     *
     * @return the key, never null.
     */
    public Value key();

    /**
     * Returns the value.
     *
     * @return the value, never null.
     */
    public Value value();
}
