package styx.data.db;

import java.util.Comparator;
import java.util.Objects;

public class Row {

    public static final Comparator<Row> ITERATION_ORDER = Comparator.comparing(Row::fullpath).thenComparing(Row::key);

    private final Path   parent;
    private final String key;
    private final int    suffix;
    private final String value;

    public Row(Path parent, String key, int suffix, String value) {
        if(value == null && suffix <= 0) {
            throw new IllegalArgumentException(); // not a valid simple row
        }
        if(value != null && suffix != 0) {
            throw new IllegalArgumentException(); // not a valid complex row
        }
        this.parent = Objects.requireNonNull(parent);
        this.key = Objects.requireNonNull(key);
        this.suffix = suffix;
        this.value = value;
    }

    @Override
    public String toString() {
        if(value == null) {
            return parent + "/" + key + "->" + suffix;
        } else {
            return parent + "/" + key + "=" + value;
        }
    }

    public boolean isComplex() {
        return value == null;
    }

    public Path fullpath() {
        return parent.add(suffix);
    }

    public Path fullpath2() {
        return isComplex() ? fullpath() : parent();
    }

    public Path parent() {
        return parent;
    }

    public String key() {
        return key;
    }

    public int suffix() {
        return suffix;
    }

    public String value() {
        return value;
    }
}
