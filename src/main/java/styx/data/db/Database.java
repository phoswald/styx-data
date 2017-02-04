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
    public int allocateSuffx(Path parent);

    public void insertComplex(Path parent, String key, int suffix);

    public void insertSimple(Path parent, String key, String value);

    public void deleteAll();

    public void deleteSingle(Path parent, String key);

    public void deleteDescendants(Path parent);
}
