package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public abstract class GenericDatabaseTest {

    private final Database testee;

    protected GenericDatabaseTest(Database testee) {
        this.testee = testee;
    }

    @Before
    public void prepare() {
        testee.insertComplex(Path.of(), "", 1);
        testee.insertSimple(Path.of(1), "key1", "val1");
        testee.insertComplex(Path.of(1), "key3", 1);
        testee.insertComplex(Path.of(1), "key4", 2);
        testee.insertSimple(Path.of(1), "key2", "val2");
        testee.insertSimple(Path.of(1, 1), "subkey2", "subval2");
        testee.insertSimple(Path.of(1, 1), "subkey1", "subval1");
    }

    @Test
    public void testSelectAll() {
        Row[] rows = testee.selectAll().toArray(Row[]::new);
        assertEquals(7, rows.length); // selectDescendants() returns a different order
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key1=val1", rows[1].toString());
        assertEquals("[1]/key2=val2", rows[2].toString());
        assertEquals("[1]/key3->1", rows[3].toString());
        assertEquals("[1]/key4->2", rows[4].toString());
        assertEquals("[1, 1]/subkey1=subval1", rows[5].toString());
        assertEquals("[1, 1]/subkey2=subval2", rows[6].toString());
    }

    @Test
    public void testSelectSingle() {
        assertEquals("[1]/key1=val1", testee.selectSingle(Path.of(1), "key1").get().toString());
        assertEquals("[1]/key3->1", testee.selectSingle(Path.of(1), "key3").get().toString());
        assertFalse(testee.selectSingle(Path.of(1), "keyX").isPresent());
    }

    @Test
    public void testSelectChildren() {
        assertEquals(1, testee.selectChildren(Path.of()).count());
        assertEquals(4, testee.selectChildren(Path.of(1)).count());
        assertEquals(2, testee.selectChildren(Path.of(1, 1)).count());
        assertEquals(0, testee.selectChildren(Path.of(1, 2)).count());
        assertEquals(0, testee.selectChildren(Path.of(2)).count());
    }

    @Test
    public void testSelectDescendants() {
        Row[] rows = testee.selectDescendants(Path.of()).toArray(Row[]::new);
        assertEquals(7, rows.length); // order is different from the one returned by selectAll()
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key1=val1", rows[1].toString());
        assertEquals("[1]/key2=val2", rows[2].toString());
        assertEquals("[1]/key3->1", rows[3].toString());
        assertEquals("[1, 1]/subkey1=subval1", rows[4].toString());
        assertEquals("[1, 1]/subkey2=subval2", rows[5].toString());
        assertEquals("[1]/key4->2", rows[6].toString());

        assertEquals(6, testee.selectDescendants(Path.of(1)).count());
        assertEquals(2, testee.selectDescendants(Path.of(1, 1)).count());
        assertEquals(0, testee.selectDescendants(Path.of(1, 2)).count());
        assertEquals(0, testee.selectDescendants(Path.of(1, 2, 3)).count());
    }

    @Test
    public void testDeleteAll() {
        testee.deleteAll();
        assertEquals(0, testee.selectAll().count());
    }

    @Test
    public void testDeleteSingle() {
        testee.deleteSingle(Path.of(1), "key1");
        assertEquals(6, testee.selectAll().count());
    }

    @Test
    public void testDeleteDescendants() {
        testee.deleteDescendants(Path.of());
        assertEquals(0, testee.selectAll().count());
    }

    @Test
    public void testDeleteDescendants2() {
        testee.deleteDescendants(Path.of(1, 1));
        Row[] rows = testee.selectAll().toArray(Row[]::new);
        assertEquals(5, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key1=val1", rows[1].toString());
        assertEquals("[1]/key2=val2", rows[2].toString());
        assertEquals("[1]/key3->1", rows[3].toString());
        assertEquals("[1]/key4->2", rows[4].toString());
    }

    @Test(expected=RuntimeException.class) // IllegalStateException if memory DB, unclear if JDBC
    public void testInsertExistingSimple() {
        testee.insertSimple(Path.of(1), "key1", "val1");
    }

    @Test(expected=RuntimeException.class) // IllegalStateException if memory DB, unclear if JDBC
    public void testInsertExistingComplex() {
        testee.insertComplex(Path.of(1), "key3", 1);
    }
}
