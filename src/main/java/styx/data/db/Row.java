package styx.data.db;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Row {

    public static final Comparator<Row> ITERATION_ORDER = Comparator.comparing(Row::fullpath).thenComparing(Row::key);

    private static final Pattern ENCODING_PATTERN = Pattern.compile("\\A([^\\t]*)\\t([^\\t]*)\\t([0-9]*)\\t([^\\t]*)\\Z");

    private final Path   parent;
    private final String key;
    private final int    suffix;
    private final String value;

    public Row(Path parent, String key, int suffix, String value) {
        this.parent = Objects.requireNonNull(parent, "Invalid row: parent must not be null.");
        this.key = Objects.requireNonNull(key, "Invalid row: key must not be null.");
        if(value != null && value.isEmpty()) {
            value = null;
        }
        if(value != null) {
            if(suffix != 0) {
                throw new IllegalArgumentException("Invalid row: suffix == 0 expected.");
            }
            this.suffix = 0;
            this.value = value;
        } else {
            if(suffix <= 0) {
                throw new IllegalArgumentException("Invalid row: suffix > 0 expected.");
            }
            this.suffix = suffix;
            this.value = null;
        }
    }

    static Row decode(String line) {
        Matcher matcher = ENCODING_PATTERN.matcher(line);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match pattern for row: " + line);
        }
        return new Row(
                Path.decode(matcher.group(1)),
                matcher.group(2),
                matcher.group(3).isEmpty() ? 0 : Integer.parseInt(matcher.group(3)),
                matcher.group(4).isEmpty() ? null : matcher.group(4));
    }

    String encode() {
        return parent.encode() + "\t" +
                key + "\t" +
                (suffix == 0 ? "" : suffix) + "\t" +
                (value == null ? "" : value);
    }

    @Override
    public String toString() {
        return "parent=" + parent +
                ", key=" + key +
                ", suffix=" + (suffix == 0 ? "" : suffix) +
                ", value=" + (value == null ? "" : value);
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
