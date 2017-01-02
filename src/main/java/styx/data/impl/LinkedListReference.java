package styx.data.impl;

import java.util.Objects;
import java.util.Optional;

import styx.data.Reference;
import styx.data.Value;

public class LinkedListReference implements Reference {

    public static final LinkedListReference ROOT = new LinkedListReference();

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
    public int compareTo(Value o) {
        throw new UnsupportedOperationException();
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
}
