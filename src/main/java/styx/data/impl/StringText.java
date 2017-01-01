package styx.data.impl;

import java.util.Objects;

import styx.data.Text;
import styx.data.Value;

public class StringText implements Text {

    private static final StringText EMPTY = new StringText("");

    private final String value;

    private StringText(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static Text valueOf(String value) {
        if(value == null || value.isEmpty()) {
            return EMPTY;
        } else {
            return new StringText(value);
        }
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toCharString() {
        return value;
    }
}
