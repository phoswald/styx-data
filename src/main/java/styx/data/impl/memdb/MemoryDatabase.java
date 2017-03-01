package styx.data.impl.memdb;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import styx.data.db.Database;
import styx.data.db.DatabaseTransaction;
import styx.data.db.Row;

class MemoryDatabase implements Database {

    private static final ConcurrentMap<String, Database> namedInstances = new ConcurrentHashMap<>();

    private final AtomicReference<SortedMap<RowKey, Row>> rows = new AtomicReference<>(new TreeMap<>());

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
        return new MemoryTransaction(rows, true);
    }

    @Override
    public DatabaseTransaction openWriteTransaction() {
        return new MemoryTransaction(rows, false);
    }
}
