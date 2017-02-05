package styx.data.impl.mem;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import styx.data.Reference;
import styx.data.Store;
import styx.data.Value;

class MemoryStore implements Store {

    private static final ConcurrentMap<String, MemoryStore> namedInstances = new ConcurrentHashMap<>();

    protected final MemoryObject root = new MemoryObject(null);

    protected MemoryStore() { }

    static Store open(String name) {
        if(name == null || name.isEmpty()) {
            return new MemoryStore();
        } else {
            return namedInstances.computeIfAbsent(name, k -> new MemoryStore());
        }
    }

    @Override
    public void close() { }

    @Override
    public Optional<Value> read(Reference ref) {
        return Optional.ofNullable(lookup(Objects.requireNonNull(ref)).read());
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
