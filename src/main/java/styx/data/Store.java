package styx.data;

import styx.data.impl.store.MemoryStore;

public interface Store extends AutoCloseable {

    public static Store open() {
        return new MemoryStore();
    }

    @Override
    public void close();

    public Value read(Reference ref);

    public void write(Reference ref, Value value);
}
