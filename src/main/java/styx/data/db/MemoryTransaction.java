package styx.data.db;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MemoryTransaction implements DatabaseTransaction {

    private final SortedMap<RowKey, Row> rows;

    MemoryTransaction(SortedMap<RowKey, Row> rows) {
        this.rows = rows;
    }

    @Override
    public void close() { }

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
        if(rows.putIfAbsent(new RowKey(row.parent(), row.key()), row) != null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void deleteAll() {
        rows.clear();
    }

    @Override
    public void deleteSingle(Path parent, String key) {
        rows.remove(new RowKey(parent, key));
    }

    @Override
    public void deleteDescendants(Path parent) {
        List<RowKey> rowkeys = rows.keySet().stream().
                filter(rk -> rk.parent.startsWith(parent)).
                collect(Collectors.toList());
        rowkeys.forEach(rows::remove);
    }
}
