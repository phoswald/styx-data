package styx.data.impl.value;

import java.util.Arrays;
import java.util.Objects;

import styx.data.Binary;
import styx.data.Kind;
import styx.data.Value;

public class ByteArrayBinary extends AbstractValue implements Binary {

    private final byte[] value;

    private static final Binary EMPTY = new ByteArrayBinary(new byte[0]);

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
    public int compareTo(Value other) {
        if(other.isBinary()) {
            return compare(this, other.asBinary());
        } else {
            return compare(kind(), other.kind());
        }
    }

    @Override
    public Kind kind() {
        return Kind.BINARY;
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

    private static int compare(Binary a, Binary b) {
        int commonByteCount = Math.min(a.byteCount(), b.byteCount());
        for(int index = 0; index < commonByteCount; index++) {
            byte byteA = a.byteAt(index);
            byte byteB = b.byteAt(index);
            if(byteA != byteB) {
                return Integer.compare(byteA & 0xFF, byteB & 0xFF);
            }
        }
        return Integer.compare(a.byteCount(), b.byteCount());
    }
}
