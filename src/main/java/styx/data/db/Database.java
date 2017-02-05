package styx.data.db;

import java.util.Optional;
import java.util.stream.Stream;

public interface Database extends AutoCloseable {

    @Override
    public void close();

    public Stream<Row> selectAll();

    public Optional<Row> selectSingle(Path parent, String key);

    public Stream<Row> selectChildren(Path parent);

    public Stream<Row> selectDescendants(Path parent);

    @Deprecated // TODO: remove allocateSuffx()
    public int allocateSuffix(Path parent);

    public default void insertComplex(Path parent, String key, int suffix) {
        insert(new Row(parent, key, suffix, null));
    }

    public default void insertSimple(Path parent, String key, String value) {
        insert(new Row(parent, key, 0, value));
    }

    public void insert(Row row);

    public void deleteAll();

    public void deleteSingle(Path parent, String key);

    public void deleteDescendants(Path parent);
}
