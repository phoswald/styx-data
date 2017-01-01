package styx.data.impl;

import java.util.Objects;
import java.util.Optional;

import styx.data.Reference;
import styx.data.Value;

public class ConcreteReference implements Reference {

    public static final ConcreteReference ROOT = new ConcreteReference();

    private final ConcreteReference parent;
    private final int partCount;
    private final Value lastPart;

    private ConcreteReference() {
        this.parent = null;
        this.partCount = 0;
        this.lastPart = null;
    }

    private ConcreteReference(ConcreteReference parent, Value lastPart) {
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
        ConcreteReference current = this;
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
        return new ConcreteReference(this, value);
    }
}
