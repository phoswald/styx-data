package styx.data.impl.memdb;

import java.util.Objects;

import styx.data.db.Path;

class RowKey implements Comparable<RowKey> {

    final Path parent;
    final String key;

    RowKey(Path parent, String key) {
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
