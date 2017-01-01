package styx.data;

import java.util.List;
import java.util.Optional;

/**
 * An immutable reference, consisting of a sequence of parts.
 */
public interface Reference extends Value {

    @Override
    public default Reference asReference() {
        return this;
    }

    /**
     * Returns the number of parts of this reference.
     *
     * @return the number of parts, zero for the root reference.
     */
    public int partCount();

    /**
     * Returns the part at the given index.
     *
     * @param index the index, must be in the range 0 .. partCount() - 1.
     * @return the given part, never null.
     * @throws IndexOutOfBoundsException if the given index is invalid.
     */
    public Value partAt(int index);

    /**
     * Returns the parent of this reference, if existing.
     *
     * @return the parent, or empty for the root reference.
     */
    public Optional<Reference> parent();

    /**
     * Returns the child reference with the given additional part.
     *
     * @param value the part to be appended to this reference, must not be null.
     * @return a reference constructed by appending the given part to this reference, never null.
     */
    public Reference child(Value value);

    public default Reference child(Value... values) {
        Reference current = this;
        if(values != null) {
            for(Value value : values) {
                current = current.child(value);
            }
        }
        return current;
    }

    public default Reference child(List<? extends Value> values) {
        Reference current = this;
        if(values != null) {
            for(Value value : values) {
                current = current.child(value);
            }
        }
        return current;
    }
}
