package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertException;

import org.junit.Test;

public class PathTest {

    @Test
    public void testEquals() {
        assertEquals(Path.of(), Path.of());
        assertEquals(Path.of(1, 2, 3), Path.of(1, 2, 3));
        assertNotEquals(Path.of(), Path.of(1));
        assertNotEquals(Path.of(1), Path.of(2));
        assertNotEquals(Path.of(1), Path.of());
        assertNotEquals(Path.of(2), Path.of(1));
        assertFalse(Path.of(1, 2, 3).equals(null));
        assertFalse(Path.of(1, 2, 3).equals("x"));
    }

    @Test
    public void testHashCode() {
        assertEquals(Path.of().hashCode(), Path.of().hashCode());
        assertEquals(Path.of(1, 2, 3).hashCode(), Path.of(1, 2, 3).hashCode());
        assertNotEquals(Path.of().hashCode(), Path.of(1).hashCode());
        assertNotEquals(Path.of(1).hashCode(), Path.of(2).hashCode());
        assertNotEquals(Path.of(1).hashCode(), Path.of().hashCode());
        assertNotEquals(Path.of(2).hashCode(), Path.of(1).hashCode());
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, Path.of().compareTo(Path.of()));
        assertEquals(0, Path.of(1, 2, 3).compareTo(Path.of(1, 2, 3)));
        assertEquals(-1, Path.of().compareTo(Path.of(1)));
        assertEquals(-1, Path.of(1).compareTo(Path.of(2)));
        assertEquals(1, Path.of(1).compareTo(Path.of()));
        assertEquals(1, Path.of(2).compareTo(Path.of(1)));
    }

    @Test
    public void testEncodeDecode64() {
        char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~".toCharArray();
        assertEquals(64, chars.length);

        for(int i = 1; i < 64; i++) {
            assertTrue(chars[i-1] < chars[i]);
        }

        for(int i = 0; i < 64; i++) {
            assertEquals("Enoder error at " + i, chars[i], Path.encode64(i));
            assertEquals("Decoder error at " + i, i, Path.decode64(chars[i]));
        }

        assertException(IllegalArgumentException.class, () -> Path.encode64(-1));
        assertException(IllegalArgumentException.class, () -> Path.encode64(64));
        assertException(IllegalArgumentException.class, () -> Path.decode64(' '));
    }

    @Test
    public void testEncodeDecode() {
        assertEncodeDecode(Path.of(), "");
        assertEncodeDecode(Path.of(0), "0");
        assertEncodeDecode(Path.of(1), "1");
        assertEncodeDecode(Path.of(9), "9");
        assertEncodeDecode(Path.of(10), "A");
        assertEncodeDecode(Path.of(35), "Z");
        assertEncodeDecode(Path.of(36), "_");
        assertEncodeDecode(Path.of(37), "aa");
        assertEncodeDecode(Path.of(63), "a~");
        assertEncodeDecode(Path.of(64), "b10");
        assertEncodeDecode(Path.of(0xFFF), "b~~");
        assertEncodeDecode(Path.of(0x1000), "c100");
        assertEncodeDecode(Path.of(0x3FFFF), "c~~~");
        assertEncodeDecode(Path.of(0x40000), "d1000");
        assertEncodeDecode(Path.of(0xFFFFFF), "d~~~~");
        assertEncodeDecode(Path.of(0x1000000), "e10000");
        assertEncodeDecode(Path.of(0x7FFFFFFF), "f1~~~~~");
        assertEncodeDecode(Path.of(0xFFFFFFFF), "f3~~~~~");
//      assertEncodeDecode(Path.of(0xFFFFFFFFFFFFFFFFL), "kF~~~~~~~~~~");
        assertEncodeDecode(Path.of(0, 1, 9, 10, 35, 36), "019AZ_");
        assertEncodeDecode(Path.of(0, 36, 0xFFF, 0x1000), "0_b~~c100");
    }

    private void assertEncodeDecode(Path path, String encoded) {
        assertEquals(encoded, path.encode());
        assertEquals(path, Path.decode(encoded));
    }
}
