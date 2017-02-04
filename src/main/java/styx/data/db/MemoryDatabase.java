package styx.data.db;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryDatabase implements Database {

    private static final ConcurrentMap<String, Database> namedInstances = new ConcurrentHashMap<>();

    private final SortedMap<RowKey, Row> rows = new TreeMap<>();

    private MemoryDatabase() { }

    public static Database open(String name) {
        if(name == null || name.isEmpty()) {
            return new MemoryDatabase();
        } else {
            return namedInstances.computeIfAbsent(name, k -> new MemoryDatabase());
        }
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
    public int allocateSuffx(Path parent) {
        return rows.values().stream().
                filter(row -> row.parent().equals(parent)).
                max(Comparator.comparing(Row::suffix)).
                map(Row::suffix).orElse(0) + 1;
    }

    @Override
    public void insertComplex(Path parent, String key, int suffix) {
        if(rows.putIfAbsent(new RowKey(parent, key), new Row(parent, key, suffix)) != null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void insertSimple(Path parent, String key, String value) {
        if(rows.putIfAbsent(new RowKey(parent, key), new Row(parent, key, value)) != null) {
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

    private static class RowKey implements Comparable<RowKey> {
        private final Path parent;
        private final String key;

        private RowKey(Path parent, String key) {
            this.parent = Objects.requireNonNull(parent);
            this.key = Objects.requireNonNull(key);
        }

        @Override
        public int compareTo(RowKey other) {
            int result = parent.compareTo(other.parent);
            if(result == 0) {
                result = key.compareTo(other.key);
            }
            return result;
        }
    }
}
