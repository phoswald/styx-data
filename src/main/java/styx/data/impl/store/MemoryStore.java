package styx.data.impl.store;

import java.util.Objects;

import styx.data.Reference;
import styx.data.Store;
import styx.data.Value;

public class MemoryStore implements Store {

    private final MemoryObject root = new MemoryObject(null);

    @Override
    public void close() { }

    @Override
    public Value read(Reference ref) {
        return lookup(Objects.requireNonNull(ref)).read();
    }

    @Override
    public void write(Reference ref, Value value) {
        lookup(Objects.requireNonNull(ref)).write(value);
    }

    private MemoryObject lookup(Reference ref) {
        MemoryObject object = root;
        int count = ref.partCount();
        for(int index = 0; index < count; index++) {
            object = object.child(ref.partAt(index));
        }
        return object;
    }
}
