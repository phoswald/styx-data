package styx.data.impl;

import java.util.Arrays;
import java.util.Objects;

import styx.data.Binary;
import styx.data.Value;

public class ByteArrayBinary implements Binary {

    private final byte[] value;

    private static final ByteArrayBinary EMPTY = new ByteArrayBinary(new byte[0]);

    private ByteArrayBinary(byte[] value) {
        this.value = Objects.requireNonNull(value);
    }

    public static Binary valueOf(byte[] value) {
        if(value == null || value.length == 0) {
            return EMPTY;
        } else {
            return new ByteArrayBinary(Arrays.copyOf(value, value.length));
        }
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int byteCount() {
        return value.length;
    }

    @Override
    public byte byteAt(int index) {
        return value[index];
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(value, value.length);
    }
}
