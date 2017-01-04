package styx.data.impl;

import java.util.Objects;

import styx.data.Kind;
import styx.data.Text;
import styx.data.Value;

public class StringText extends AbstractValue implements Text {

    private static final Text EMPTY = new StringText("");

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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for(int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch(character) {
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '"':  sb.append("\\\""); break;
                default:   sb.append(character); break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    @Override
    public int compareTo(Value other) {
        if(other.isText()) {
            return toCharString().compareTo(other.asText().toCharString());
        } else {
            return compare(kind(), other.kind());
        }
    }

    @Override
    public Kind kind() {
        return Kind.TEXT;
    }

    @Override
    public String toCharString() {
        return value;
    }
}
