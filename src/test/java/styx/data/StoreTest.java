package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static styx.data.Values.complex;
import static styx.data.Values.empty;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.parse;
import static styx.data.Values.reference;
import static styx.data.Values.root;
import static styx.data.Values.text;

import org.junit.Test;

public class StoreTest {

    @Test
    public void testRead() {
        try(Store store = Store.open()) {
            assertNull(store.read(root()));

            store.write(root(), list(text("val1"), text("val2")));

            assertEquals(list(text("val1"), text("val2")), store.read(root()));
            assertEquals(text("val1"), store.read(reference(number(1))));
            assertEquals(text("val1"), store.read(reference(number(1))));
            assertEquals(text("val2"), store.read(reference(number(2))));
            assertNull(store.read(reference(number(3))));

            store.write(reference(number(3)), empty());
            store.write(reference(number(4)), empty());

            assertEquals(list(text("val1"), text("val2"), empty(), empty()), store.read(root()));

            store.write(root(), text("tip"));
            store.write(root(), text("top"));

            assertEquals(null, store.read(reference(number(1), number(11))));
            assertEquals(text("top"), store.read(root()));
        }
    }

    @Test
    public void testWrite1() throws StyxException {
        try(Store store = Store.open()) {
            store.write(root(), null);
            assertNull(store.read(root()));

            store.write(root(), complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))));

            assertEquals(text("v2"), store.read(reference(text("v1"))));
            assertNull(store.read(reference(text("v2"))));
            assertEquals(text("v4"), store.read(reference(text("v3"))));
            assertNull(store.read(reference(text("v3"), text("v1"))));

            store.write(root(), null);

            assertNull(store.read(root()));
            assertNull(store.read(reference(text("v1"))));
            assertNull(store.read(reference(text("v2"))));
            assertNull(store.read(reference(text("v3"), text("v1"))));
            assertNull(store.read(reference(text("v3"), text("v2"))));

            store.write(root(), complex());
            store.write(reference(text("v1")), text("v2"));
            store.write(reference(text("v2")), text("v1"));

            assertEquals(complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v2"), text("v1"))), store.read(root()));
        }
    }

    @Test
    public void testWrite2() throws StyxException {
        try(Store store = Store.open()) {
            store.write(root(), complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))));

            store.write(reference(text("v1")), complex());
            store.write(reference(text("v1"), text("v2")), text("v3"));

            assertEquals(text("v3"), store.read(reference(text("v1"), text("v2"))));
            assertEquals(complex(text("v2"), text("v3")), store.read(reference(text("v1"))));
            assertEquals(complex(
                    pair(text("v1"), complex(text("v2"), text("v3"))),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))), store.read(root()));
        }
    }

    @Test
    public void testWrite3() throws StyxException {
        try(Store store = Store.open()) {
            store.write(root(), complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))));

            store.write(reference(text("v1")), complex());
            store.write(reference(text("v1"), text("v1")), complex());
            store.write(reference(text("v1"), text("v1"), text("v1")), complex());
            store.write(reference(text("v1"), text("v1"), text("v1"), text("v1")), text("v1"));
            store.write(reference(text("v3")), complex());
            store.write(reference(text("v3"), text("v3")), complex());
            store.write(reference(text("v3"), text("v3"), text("v3")), complex());
            store.write(reference(text("v3"), text("v3"), text("v3"), text("v3")), text("v3"));
            store.write(reference(text("v5")), null);
            assertEquals(parse("{v1: v1 v1 v1 v1, v3: v3 v3 v3 v3}"), store.read(root()));

            store.write(reference(text("v1"), text("v1"), text("v1"), text("v1")), null);
            store.write(reference(text("v3"), text("v3"), text("v3"), text("v3")), null);
            store.write(reference(text("v5")), text("v3"));

            assertEquals(complex(text("v1"), empty()), store.read(reference(text("v1"), text("v1"))));
            assertEquals(parse("{v1: v1 v1 {}, v3: v3 v3 {}, v5: v3}"), store.read(root()));
            assertEquals(complex(text("v3"), empty()), store.read(reference(text("v3"), text("v3"))));

            store.write(root(), text("xxx"));
            store.write(root(), text("xxx"));
            assertEquals(text("xxx"), store.read(root()));
            assertNull(store.read(reference(text("v1"), text("v1"))));
            assertNull(store.read(reference(text("v3"), text("v3"))));
        }
    }

    @Test
    public void testWriteSub1() throws StyxException {
        try(Store store = Store.open()) {
            store.write(root(), list(text("val1"), text("val2"), text("val3")));
            try {
                store.write(reference(text("x"), text("y"), text("z")), null);
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
            try {
                store.write(reference(text("x"), text("y"), text("z")), empty());
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
        }
    }

    @Test
    public void testWriteSub2() throws StyxException {
        try(Store store = Store.open()) {
            store.write(root(), text("xxx"));
            try {
                store.write(reference(text("x"), text("y"), text("z")), null);
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
            try {
                store.write(reference(text("x")), empty());
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-complex value.", e.getMessage());
            }
        }
    }
}