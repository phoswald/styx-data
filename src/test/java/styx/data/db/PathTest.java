package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

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
}
