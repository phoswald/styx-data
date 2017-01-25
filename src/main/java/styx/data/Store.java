package styx.data;

import java.nio.file.Path;

import styx.data.impl.store.FileStore;
import styx.data.impl.store.MemoryStore;

public interface Store extends AutoCloseable {

    public static Store memory() {
        return memory(null);
    }

    public static Store memory(String name) {
        return MemoryStore.open(name);
    }

    public static Store file(Path file) {
        return FileStore.open(file);
    }

    @Override
    public void close();

    public Value read(Reference ref);

    public void write(Reference ref, Value value);
}
