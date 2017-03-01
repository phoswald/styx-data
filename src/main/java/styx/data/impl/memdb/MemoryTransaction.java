package styx.data.impl.memdb;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import styx.data.db.DatabaseTransaction;
import styx.data.db.Path;
import styx.data.db.Row;

class MemoryTransaction implements DatabaseTransaction {

    private final AtomicReference<SortedMap<RowKey, Row>> ref;
    private final SortedMap<RowKey, Row> rows;
    private final boolean readOnly;
    private boolean commit;

    MemoryTransaction(AtomicReference<SortedMap<RowKey, Row>> ref, boolean readOnly) {
        this.ref = ref;
        this.rows = readOnly ? ref.get() : new TreeMap<>(ref.get()); // fast for read, slow for write transactions!
        this.readOnly = readOnly;
        this.commit = false;
    }

    @Override
    public void close() {
        if(!readOnly && commit) {
            ref.set(rows);
        }
    }

    @Override
    public void markCommit() {
        commit = true;
    }

    @Override
    public Stream<Row> selectAll() {
        return rows.values().stream();
    }

    @Override
    public Optional<Row> selectSingle(Path parent, String key) {
        return Optional.ofNullable(rows.get(new RowKey(parent, key)));
    }

    @Override
    public Stream<Row> selectChildren(Path parent) {
        return selectAll().filter(row -> row.parent().equals(parent));
    }

    @Override
    public Stream<Row> selectDescendants(Path parent) {
        return selectAll().
                filter(row -> row.parent().startsWith(parent)).
                sorted(Row.ITERATION_ORDER);
    }

    @Override
    public int allocateSuffix(Path parent) {
        return rows.values().stream().
                filter(row -> row.parent().equals(parent)).
                max(Comparator.comparing(Row::suffix)).
                map(Row::suffix).orElse(0) + 1;
    }

    @Override
    public void insert(Row row) {
        if(readOnly) {
            throw new IllegalStateException();
        }
        if(rows.putIfAbsent(new RowKey(row.parent(), row.key()), row) != null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void deleteAll() {
        if(readOnly) {
            throw new IllegalStateException();
        }
        rows.clear();
    }

    @Override
    public void deleteSingle(Path parent, String key) {
        if(readOnly) {
            throw new IllegalStateException();
        }
        rows.remove(new RowKey(parent, key));
    }

    @Override
    public void deleteDescendants(Path parent) {
        if(readOnly) {
            throw new IllegalStateException();
        }
        List<RowKey> rowkeys = rows.keySet().stream().
                filter(rk -> rk.parent.startsWith(parent)).
                collect(Collectors.toList());
        rowkeys.forEach(rows::remove);
    }
}
