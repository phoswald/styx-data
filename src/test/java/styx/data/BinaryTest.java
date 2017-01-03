package styx.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.binary;

import org.junit.Test;

public class BinaryTest {

    @Test
    public void binary_bytes_success() {
        Binary value = binary(new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD });
        assertNotNull(value);
        assertEquals(4, value.byteCount());
        assertEquals(0x12, value.byteAt(1));
        assertException(IndexOutOfBoundsException.class, () -> value.byteAt(5));
        assertArrayEquals(new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD }, value.toByteArray());
    }

    @Test
    public void binary_empty_success() {
        Binary value = binary(new byte[0]);
        assertNotNull(value);
        assertSame(value, binary(new byte[0]));
        assertArrayEquals(new byte[0], value.toByteArray());
    }

    @Test
    public void binary_null_success() {
        Binary value = binary(null);
        assertNotNull(value);
        assertSame(value, binary(new byte[0]));
        assertArrayEquals(new byte[0], value.toByteArray());
    }

    @Test
    public void binary_noArg_success() {
        Binary value = binary();
        assertNotNull(value);
        assertSame(value, binary(new byte[0]));
        assertArrayEquals(new byte[0], value.toByteArray());
    }

    @Test
    public void binary_modifiedAfterNew_notChanged() {
        byte[] bytes = new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD };
        Binary value = binary(bytes);
        bytes[1] = 0x13;
        assertEquals(0x12, value.byteAt(1));
    }

    @Test
    public void toByteArray_modified_notChanged() {
        Binary value = binary(new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD });
        value.toByteArray()[1] = 0x13;
        assertEquals(0x12, value.byteAt(1));
    }

}
