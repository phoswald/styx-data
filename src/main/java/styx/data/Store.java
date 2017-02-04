package styx.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import styx.data.db.DatabaseStore;
import styx.data.db.MemoryDatabase;
import styx.data.impl.mem.FileStore;
import styx.data.impl.mem.MemoryStore;

public interface Store extends AutoCloseable {

    public static Store open(String uri) {
        if(uri.equals("memory")) {
            return memory();
        }
        if(uri.startsWith("memory:")) {
            return memory(uri.substring(7));
        }
        if(uri.startsWith("file:")) {
            return file(Paths.get(uri.substring(5)));
        }
        if(uri.equals("memorydb")) {
            return memorydb();
        }
        if(uri.startsWith("memorydb:")) {
            return memorydb(uri.substring(9));
        }
        throw new IllegalArgumentException("Invalid uri: " + uri);
    }

    public static Store memory() {
        return memory(null);
    }

    public static Store memory(String name) {
        return MemoryStore.open(name);
    }

    public static Store file(Path file) {
        return FileStore.open(file);
    }

    public static Store memorydb() {
        return memorydb(null);
    }

    public static Store memorydb(String name) {
        return new DatabaseStore(MemoryDatabase.open(name));
    }

    @Override
    public void close();

    public Optional<Value> read(Reference ref);

    public void write(Reference ref, Value value);
}
