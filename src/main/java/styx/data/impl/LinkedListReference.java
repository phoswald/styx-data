package styx.data.impl;

import java.util.Objects;
import java.util.Optional;

import styx.data.Reference;
import styx.data.Value;

/**
 * An implementation of a reference value as an immutable, singly linked list.
 * <p>
 * Users never create instances directly. Instead, references can be constructed by starting with the
 * empty node (which is exposed by the public static field ROOT) and using the child() method to add parts.
 */
public class LinkedListReference extends AbstractValue implements Reference {

    /** the starting point for working with reference values */
    public static final Reference ROOT = new LinkedListReference();

    private final LinkedListReference parent;
    private final int partCount;
    private final Value lastPart;

    private LinkedListReference() {
        this.parent = null;
        this.partCount = 0;
        this.lastPart = null;
    }

    private LinkedListReference(LinkedListReference parent, Value lastPart) {
        this.parent = Objects.requireNonNull(parent);
        this.partCount = parent.partCount + 1;
        this.lastPart = Objects.requireNonNull(lastPart);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for(int index = 0; index < partCount; index++) {
            if(index > 0) {
                sb.append(',');
            }
            sb.append(partAt(index).toString());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(Value other) {
        if(other.isReference()) {
            return compare(this, other.asReference());
        } else if(other.isComplex()) {
            return -1; // reference sorts before complex.
        } else {
            return 1; // reference sorts after all other values exception complex.
        }
    }

    @Override
    public int partCount() {
        return partCount;
    }

    @Override
    public Value partAt(int index) {
        if(index < 0 || index >= partCount) {
            throw new IndexOutOfBoundsException();
        }
        LinkedListReference current = this;
        while(index + 1 < current.partCount) {
            current = current.parent;
        }
        return current.lastPart;
    }

    @Override
    public Optional<Reference> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Reference child(Value value) {
        return new LinkedListReference(this, value);
    }

    private static int compare(Reference a, Reference b) {
        int commonByteCount = Math.min(a.partCount(), b.partCount());
        for(int index = 0; index < commonByteCount; index++) {
            int order = a.partAt(index).compareTo(b.partAt(index));
            if(order != 0) {
                return order;
            }
        }
        return Integer.compare(a.partCount(), b.partCount());
    }
}
