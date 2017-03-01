package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GenericDatabaseTest {

    private final Database testee;

    protected GenericDatabaseTest(Database testee) {
        this.testee = testee;
    }

    @Before
    public void prepare() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.deleteAll();
            txn.insertComplex(Path.of(), "", 1);
            txn.insertSimple(Path.of(1), "key1", "val1");
            txn.insertComplex(Path.of(1), "key3", 1);
            txn.insertComplex(Path.of(1), "key4", 2);
            txn.insertSimple(Path.of(1), "key2", "val2");
            txn.insertSimple(Path.of(1, 1), "subkey2", "subval2");
            txn.insertSimple(Path.of(1, 1), "subkey1", "subval1");
        }
    }

    @After
    public void cleanup() {
        testee.close();
    }

    @Test
    public void testSelectAll() {
        try(DatabaseTransaction txn = testee.openReadTransaction()) {
            Row[] rows = txn.selectAll().toArray(Row[]::new);
            assertEquals(7, rows.length); // selectDescendants() returns a different order
            assertEquals("parent=[], key=, suffix=1, value=", rows[0].toString());
            assertEquals("parent=[1], key=key1, suffix=, value=val1", rows[1].toString());
            assertEquals("parent=[1], key=key2, suffix=, value=val2", rows[2].toString());
            assertEquals("parent=[1], key=key3, suffix=1, value=", rows[3].toString());
            assertEquals("parent=[1], key=key4, suffix=2, value=", rows[4].toString());
            assertEquals("parent=[1, 1], key=subkey1, suffix=, value=subval1", rows[5].toString());
            assertEquals("parent=[1, 1], key=subkey2, suffix=, value=subval2", rows[6].toString());
        }
    }

    @Test
    public void testSelectSingle() {
        try(DatabaseTransaction txn = testee.openReadTransaction()) {
            assertEquals("parent=[1], key=key1, suffix=, value=val1", txn.selectSingle(Path.of(1), "key1").get().toString());
            assertEquals("parent=[1], key=key3, suffix=1, value=", txn.selectSingle(Path.of(1), "key3").get().toString());
            assertFalse(txn.selectSingle(Path.of(1), "keyX").isPresent());
        }
    }

    @Test
    public void testSelectChildren() {
        try(DatabaseTransaction txn = testee.openReadTransaction()) {
            assertEquals(1, txn.selectChildren(Path.of()).count());
            assertEquals(4, txn.selectChildren(Path.of(1)).count());
            assertEquals(2, txn.selectChildren(Path.of(1, 1)).count());
            assertEquals(0, txn.selectChildren(Path.of(1, 2)).count());
            assertEquals(0, txn.selectChildren(Path.of(2)).count());
        }
    }

    @Test
    public void testSelectDescendants() {
        try(DatabaseTransaction txn = testee.openReadTransaction()) {
            Row[] rows = txn.selectDescendants(Path.of()).toArray(Row[]::new);
            assertEquals(7, rows.length); // order is different from the one returned by selectAll()
            assertEquals("parent=[], key=, suffix=1, value=", rows[0].toString());
            assertEquals("parent=[1], key=key1, suffix=, value=val1", rows[1].toString());
            assertEquals("parent=[1], key=key2, suffix=, value=val2", rows[2].toString());
            assertEquals("parent=[1], key=key3, suffix=1, value=", rows[3].toString());
            assertEquals("parent=[1, 1], key=subkey1, suffix=, value=subval1", rows[4].toString());
            assertEquals("parent=[1, 1], key=subkey2, suffix=, value=subval2", rows[5].toString());
            assertEquals("parent=[1], key=key4, suffix=2, value=", rows[6].toString());

            assertEquals(6, txn.selectDescendants(Path.of(1)).count());
            assertEquals(2, txn.selectDescendants(Path.of(1, 1)).count());
            assertEquals(0, txn.selectDescendants(Path.of(1, 2)).count());
            assertEquals(0, txn.selectDescendants(Path.of(1, 2, 3)).count());
        }
    }

    @Test
    public void testDeleteAll() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.deleteAll();
            assertEquals(0, txn.selectAll().count());
        }
    }

    @Test
    public void testDeleteSingle() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.deleteSingle(Path.of(1), "key1");
            assertEquals(6, txn.selectAll().count());
        }
    }

    @Test
    public void testDeleteDescendants() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.deleteDescendants(Path.of());
            assertEquals(0, txn.selectAll().count());
        }
    }

    @Test
    public void testDeleteDescendants2() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.deleteDescendants(Path.of(1, 1));
            Row[] rows = txn.selectAll().toArray(Row[]::new);
            assertEquals(5, rows.length);
            assertEquals("parent=[], key=, suffix=1, value=", rows[0].toString());
            assertEquals("parent=[1], key=key1, suffix=, value=val1", rows[1].toString());
            assertEquals("parent=[1], key=key2, suffix=, value=val2", rows[2].toString());
            assertEquals("parent=[1], key=key3, suffix=1, value=", rows[3].toString());
            assertEquals("parent=[1], key=key4, suffix=2, value=", rows[4].toString());
        }
    }

    @Test(expected=RuntimeException.class) // IllegalStateException if memory DB, unclear if JDBC
    public void testInsertExistingSimple() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.insertSimple(Path.of(1), "key1", "val1");
        }
    }

    @Test(expected=RuntimeException.class) // IllegalStateException if memory DB, unclear if JDBC
    public void testInsertExistingComplex() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            txn.insertComplex(Path.of(1), "key3", 1);
        }
    }

    @Test
    public void testCaseSensitivity() {
        try(DatabaseTransaction txn = testee.openWriteTransaction()) {
            // key must be case sensitive (and not unique)
            assertFalse(txn.selectSingle(Path.of(1), "KEY1").isPresent());
            txn.insertSimple(Path.of(1), "KEY1", "UPPER");
            assertTrue(txn.selectSingle(Path.of(1), "KEY1").isPresent());
            assertEquals("parent=[1], key=KEY1, suffix=, value=UPPER", txn.selectSingle(Path.of(1), "KEY1").get().toString());

            // keys that differ in case must be separate
            Row[] rowsTop = txn.selectChildren(Path.of(1)).toArray(Row[]::new);
            assertEquals(5, rowsTop.length);
            assertEquals("parent=[1], key=KEY1, suffix=, value=UPPER", rowsTop[0].toString());
            assertEquals("parent=[1], key=key1, suffix=, value=val1", rowsTop[1].toString());
            assertEquals("parent=[1], key=key2, suffix=, value=val2", rowsTop[2].toString());

            // prepare paths that differ in case
            Path pathLower = Path.decode("1bxy");
            Path pathUpper = Path.decode("1bXY");
            txn.insertSimple(pathLower, "keyLower", "valueLower");
            txn.insertSimple(pathUpper, "keyUpper", "valueUpper");

            // '=' on parent must be case sensitive
            Row[] rowsLower = txn.selectChildren(pathLower).toArray(Row[]::new);
            Row[] rowsUpper = txn.selectChildren(pathUpper).toArray(Row[]::new);
            assertEquals(1, rowsLower.length);
            assertEquals(1, rowsUpper.length);
            assertEquals("parent=[1, 3901], key=keyLower, suffix=, value=valueLower", rowsLower[0].toString());
            assertEquals("parent=[1, 2146], key=keyUpper, suffix=, value=valueUpper", rowsUpper[0].toString());

            // 'LIKE' on parent must be case sensitive
            rowsLower = txn.selectDescendants(pathLower).toArray(Row[]::new);
            rowsUpper = txn.selectDescendants(pathUpper).toArray(Row[]::new);
            assertEquals(1, rowsLower.length);
            assertEquals(1, rowsUpper.length);
            assertEquals("parent=[1, 3901], key=keyLower, suffix=, value=valueLower", rowsLower[0].toString());
            assertEquals("parent=[1, 2146], key=keyUpper, suffix=, value=valueUpper", rowsUpper[0].toString());
        }
    }
}
