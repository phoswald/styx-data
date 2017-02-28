package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static styx.data.AssertUtils.assertException;

import org.junit.Test;

public class RowTest {

    @Test
    public void testConstructorSimple() {
        Row row = new Row(Path.of(), "", 1, null);
        Row row2 = new Row(Path.of(), "", 1, "");
        assertNotNull(row.parent());
        assertNotNull(row.key());
        assertNull(row.value());
        assertNull(row2.value());
    }

    @Test
    public void testConstructorComplex() {
        Row row = new Row(Path.of(), "", 0, "x");
        assertNotNull(row.parent());
        assertNotNull(row.key());
        assertNotNull(row.value());
    }

    @Test
    public void testConstructorInvalid() {
        assertException(NullPointerException.class, "Invalid row: parent must not be null.", () -> new Row(null, "", 1, null));
        assertException(NullPointerException.class, "Invalid row: key must not be null.", () -> new Row(Path.of(), null, 1, null));
        assertException(IllegalArgumentException.class, "Invalid row: suffix == 0 expected.", () -> new Row(Path.of(), "", 1, "x"));
        assertException(IllegalArgumentException.class, "Invalid row: suffix > 0 expected.", () -> new Row(Path.of(), "", 0, ""));
    }

    @Test
    public void testEncodeDecode() {
        assertEncodeDecode(new Row(Path.of(1,2,3), "key", 0, "value"), "123\tkey\t\tvalue");
        assertEncodeDecode(new Row(Path.of(1,2,3), "key", 456, null), "123\tkey\t456\t");
        assertEncodeDecode(new Row(Path.of(1,2,3), "key", 456, ""), "123\tkey\t456\t");
    }

    @Test
    public void testDecodeInvalid() {
        assertException(IllegalArgumentException.class, "Line does not match pattern for row: xxxx", () -> Row.decode("xxxx"));
    }

    private void assertEncodeDecode(Row row, String encoded) {
        assertEquals(encoded, row.encode());
        assertEquals(row.toString(), Row.decode(encoded).toString());
    }
}
