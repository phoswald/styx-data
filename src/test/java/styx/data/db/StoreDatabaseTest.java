package styx.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static styx.data.Values.complex;
import static styx.data.Values.empty;
import static styx.data.Values.list;
import static styx.data.Values.pair;
import static styx.data.Values.reference;
import static styx.data.Values.root;
import static styx.data.Values.text;

import org.junit.Test;

import styx.data.Store;

public class StoreDatabaseTest {

    private final Database db = MemoryDatabase.open(null);

    @Test
    public void read_rootEmpty_noValue() {
        try(Store store = new DatabaseStore(db)) {
            assertNull(store.read(root()).orElse(null));
        }
    }

    @Test
    public void read_rootSimple_found() {
        db.insertSimple(Path.of(), "", "value");
        try(Store store = new DatabaseStore(db)) {
            assertEquals(text("value"), store.read(root()).orElse(null));
        }
    }

    @Test
    public void read_rootEmptyComplex_found() {
        db.insertComplex(Path.of(), "", 1);
        try(Store store = new DatabaseStore(db)) {
            assertEquals(complex(), store.read(root()).orElse(null));
        }
    }

    @Test
    public void read_referenceEmpty_noValue() {
        try(Store store = new DatabaseStore(db)) {
            assertNull(store.read(reference(text("key"), text("subkey"))).orElse(null));
        }
    }

    @Test
    public void read_referenceSimple_found() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertSimple(Path.of(1, 2), "subkey", "value");
        try(Store store = new DatabaseStore(db)) {
            assertEquals(text("value"), store.read(reference(text("key"), text("subkey"))).orElse(null));
        }
    }

    @Test
    public void read_referenceComplex_found() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertComplex(Path.of(1, 2), "subkey", 3);
        db.insertSimple(Path.of(1, 2, 3), "1", "value1");
        db.insertSimple(Path.of(1, 2, 3), "2", "value2");
        try(Store store = new DatabaseStore(db)) {
            assertEquals(
                    list(text("value1"), text("value2")),
                    store.read(reference(text("key"), text("subkey"))).orElse(null));
        }
        try(Store store = new DatabaseStore(db)) {
            assertEquals(
                    complex(text("subkey"), list(text("value1"), text("value2"))),
                    store.read(reference(text("key"))).orElse(null));
        }
        try(Store store = new DatabaseStore(db)) {
            assertEquals(
                    complex(text("key"), complex(text("subkey"), list(text("value1"), text("value2")))),
                    store.read(reference()).orElse(null));
        }
    }

    @Test
    public void read_referenceComplex1_found() {
        db.insertComplex(Path.of(), "", 1);
        db.insertSimple(Path.of(1), "1", "val1");
        db.insertSimple(Path.of(1), "2", "val2");
        db.insertComplex(Path.of(1), "3", 1);
        db.insertComplex(Path.of(1), "4", 2);
        try(Store store = new DatabaseStore(db)) {
            assertEquals(list(text("val1"), text("val2"), empty(), empty()), store.read(root()).orElse(null));
        }
    }

    @Test
    public void read_referenceComplex2_found() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "A", 1);
        db.insertComplex(Path.of(1, 1), "AA", 1);
        db.insertComplex(Path.of(1, 1, 1), "AAA", 1);
        db.insertSimple(Path.of(1, 1, 1, 1), "keyA1", "valueA1");
        db.insertSimple(Path.of(1, 1, 1, 1), "keyA2", "valueA2");
        db.insertComplex(Path.of(1), "B", 2);
        db.insertComplex(Path.of(1, 2), "BA", 1);
        db.insertComplex(Path.of(1, 2, 1), "BAA", 1);
        db.insertSimple(Path.of(1, 2, 1, 1), "keyB1", "valueB1");
        db.insertSimple(Path.of(1, 2, 1, 1), "keyB2", "valueB2");
        try(Store store = new DatabaseStore(db)) {
            assertEquals(complex(
                        pair(text("A"), complex(text("AA"), complex(text("AAA"),
                            complex(pair(text("keyA1"), text("valueA1")), pair(text("keyA2"), text("valueA2")))))),
                        pair(text("B"), complex(text("BA"), complex(text("BAA"),
                            complex(pair(text("keyB1"), text("valueB1")), pair(text("keyB2"), text("valueB2"))))))),
                    store.read(reference()).orElse(null));
        }
    }

    @Test
    public void write_rootSimple_inserted() {
        try(Store store = new DatabaseStore(db)) {
            store.write(root(), text("value"));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(1, rows.length);
        assertEquals("[]/=value", rows[0].toString());
    }

    @Test
    public void write_rootComplex_inserted() {
        try(Store store = new DatabaseStore(db)) {
            store.write(root(), complex(pair(text("key1"), text("val1")), pair(text("key2"), list(text("val2A"), text("val2B")))));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(5, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key1=val1", rows[1].toString());
        assertEquals("[1]/key2->1", rows[2].toString());
        assertEquals("[1, 1]/1=val2A", rows[3].toString());
        assertEquals("[1, 1]/2=val2B", rows[4].toString());
    }

    @Test
    public void write_rootNullSimple_deleted() {
        db.insertSimple(Path.of(), "", "value");
        try(Store store = new DatabaseStore(db)) {
            store.write(root(), null);
        }
        assertEquals(0, db.selectAll().count());
    }

    @Test
    public void write_rootNullComplex_deleted() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertComplex(Path.of(1, 2), "subkey", 3);
        db.insertSimple(Path.of(1, 2, 3), "1", "value1");
        db.insertSimple(Path.of(1, 2, 3), "2", "value2");
        try(Store store = new DatabaseStore(db)) {
            store.write(root(), null);
        }
        assertEquals(0, db.selectAll().count());
    }

    @Test
    public void write_referenceSimple_replaced() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertComplex(Path.of(1, 2), "subkey", 3);
        db.insertSimple(Path.of(1, 2, 3), "1", "value1");
        db.insertSimple(Path.of(1, 2, 3), "2", "value2");
        try(Store store = new DatabaseStore(db)) {
            store.write(reference(text("key"), text("subkey")), text("newvalue"));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(3, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key->2", rows[1].toString());
        assertEquals("[1, 2]/subkey=newvalue", rows[2].toString());
    }

    @Test
    public void write_referenceComplex_replaced() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertComplex(Path.of(1, 2), "subkey", 3);
        db.insertSimple(Path.of(1, 2, 3), "1", "value1");
        db.insertSimple(Path.of(1, 2, 3), "2", "value2");
        try(Store store = new DatabaseStore(db)) {
            store.write(reference(text("key"), text("subkey")), complex(text("newkey"), text("newvalue")));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(4, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key->2", rows[1].toString());
        assertEquals("[1, 2]/subkey->1", rows[2].toString());
        assertEquals("[1, 2, 1]/newkey=newvalue", rows[3].toString());
    }

    @Test
    public void write_referenceComplex2_replaced() {
        db.insertComplex(Path.of(), "", 1);
        db.insertComplex(Path.of(1), "key", 2);
        db.insertSimple(Path.of(1, 2), "subkey", "oldvalue");
        try(Store store = new DatabaseStore(db)) {
            store.write(reference(text("key"), text("subkey")), complex(text("newkey"), text("newvalue")));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(4, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key->2", rows[1].toString());
        assertEquals("[1, 2]/subkey->1", rows[2].toString());
        assertEquals("[1, 2, 1]/newkey=newvalue", rows[3].toString());
    }

    @Test
    public void write_multipleChildren_inserted() {
        try(Store store = new DatabaseStore(db)) {
            store.write(root(), complex());
            store.write(reference(text("key1")), text("val1"));
            store.write(reference(text("key2")), text("val2"));
            store.write(reference(text("key3")), complex());
            store.write(reference(text("key4")), complex(text("k"), text("v")));
        }
        Row[] rows = db.selectAll().toArray(Row[]::new);
        assertEquals(6, rows.length);
        assertEquals("[]/->1", rows[0].toString());
        assertEquals("[1]/key1=val1", rows[1].toString());
        assertEquals("[1]/key2=val2", rows[2].toString());
        assertEquals("[1]/key3->1", rows[3].toString());
        assertEquals("[1]/key4->2", rows[4].toString());
        assertEquals("[1, 2]/k=v", rows[5].toString());
    }
}