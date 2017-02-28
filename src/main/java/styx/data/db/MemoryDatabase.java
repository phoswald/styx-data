package styx.data.db;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class MemoryDatabase implements Database {

    private static final ConcurrentMap<String, Database> namedInstances = new ConcurrentHashMap<>();

    private final SortedMap<RowKey, Row> rows = new TreeMap<>();

    private MemoryDatabase() { }

    static Database open(String name) {
        if(name == null || name.isEmpty()) {
            return new MemoryDatabase();
        } else {
            return namedInstances.computeIfAbsent(name, k -> new MemoryDatabase());
        }
    }

    @Override
    public void close() { }

    @Override
    public DatabaseTransaction openReadTransaction() {
        return openWriteTransaction();
    }

    @Override
    public DatabaseTransaction openWriteTransaction() {
        return new MemoryTransaction(rows);
    }
}
