package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static styx.data.AssertUtils.assertException;
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

import styx.data.exception.InvalidWriteException;

public abstract class GenericStoreTest {

    protected final String url;

    protected GenericStoreTest(String url) {
        this.url = url;
    }

    @Test
    public void testRead() {
        try(Store store = Store.open(url)) {
            assertNull(store.read(root()).orElse(null));

            store.write(root(), list(text("val1"), text("val2")));

            assertEquals(list(text("val1"), text("val2")), store.read(root()).orElse(null));
            assertEquals(text("val1"), store.read(reference(number(1))).orElse(null));
            assertEquals(text("val1"), store.read(reference(number(1))).orElse(null));
            assertEquals(text("val2"), store.read(reference(number(2))).orElse(null));
            assertNull(store.read(reference(number(3))).orElse(null));

            store.write(reference(number(3)), empty());
            store.write(reference(number(4)), empty());

            assertEquals(list(text("val1"), text("val2"), empty(), empty()), store.read(root()).orElse(null));

            store.write(root(), text("tip"));
            store.write(root(), text("top"));

            assertEquals(null, store.read(reference(number(1), number(11))).orElse(null));
            assertEquals(text("top"), store.read(root()).orElse(null));
        }
    }

    @Test
    public void testWrite1() {
        try(Store store = Store.open(url)) {
            store.write(root(), null);
            assertNull(store.read(root()).orElse(null));

            store.write(root(), complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))));

            assertEquals(text("v2"), store.read(reference(text("v1"))).orElse(null));
            assertNull(store.read(reference(text("v2"))).orElse(null));
            assertEquals(text("v4"), store.read(reference(text("v3"))).orElse(null));
            assertNull(store.read(reference(text("v3"), text("v1"))).orElse(null));

            store.write(root(), null);

            assertNull(store.read(root()).orElse(null));
            assertNull(store.read(reference(text("v1"))).orElse(null));
            assertNull(store.read(reference(text("v2"))).orElse(null));
            assertNull(store.read(reference(text("v3"), text("v1"))).orElse(null));
            assertNull(store.read(reference(text("v3"), text("v2"))).orElse(null));

            store.write(root(), complex());
            store.write(reference(text("v1")), text("v2"));
            store.write(reference(text("v2")), text("v1"));

            assertEquals(complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v2"), text("v1"))), store.read(root()).orElse(null));
        }
    }

    @Test
    public void testWrite2() {
        try(Store store = Store.open(url)) {
            store.write(root(), complex(
                    pair(text("v1"), text("v2")),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))));

            store.write(reference(text("v1")), complex());
            store.write(reference(text("v1"), text("v2")), text("v3"));

            assertEquals(text("v3"), store.read(reference(text("v1"), text("v2"))).orElse(null));
            assertEquals(complex(text("v2"), text("v3")), store.read(reference(text("v1"))).orElse(null));
            assertEquals(complex(
                    pair(text("v1"), complex(text("v2"), text("v3"))),
                    pair(text("v3"), text("v4")),
                    pair(text("v5"), text("v6"))), store.read(root()).orElse(null));
        }
    }

    @Test
    public void testWrite3() {
        try(Store store = Store.open(url)) {
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
            assertEquals(parse("{v1: v1 v1 v1 v1, v3: v3 v3 v3 v3}"), store.read(root()).orElse(null));

            store.write(reference(text("v1"), text("v1"), text("v1"), text("v1")), null);
            store.write(reference(text("v3"), text("v3"), text("v3"), text("v3")), null);
            store.write(reference(text("v5")), text("v3"));

            assertEquals(complex(text("v1"), empty()), store.read(reference(text("v1"), text("v1"))).orElse(null));
            assertEquals(parse("{v1: v1 v1 {}, v3: v3 v3 {}, v5: v3}"), store.read(root()).orElse(null));
            assertEquals(complex(text("v3"), empty()), store.read(reference(text("v3"), text("v3"))).orElse(null));

            store.write(root(), text("xxx"));
            store.write(root(), text("xxx"));
            assertEquals(text("xxx"), store.read(root()).orElse(null));
            assertNull(store.read(reference(text("v1"), text("v1"))).orElse(null));
            assertNull(store.read(reference(text("v3"), text("v3"))).orElse(null));
        }
    }

    @Test
    public void testWriteSub1() {
        try(Store store = Store.open(url)) {
            store.write(root(), list(text("val1"), text("val2"), text("val3")));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), null));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), empty()));
        }
    }

    @Test
    public void testWriteSub2() {
        try(Store store = Store.open(url)) {
            store.write(root(), text("xxx"));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), null));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-complex value.",
                    () -> store.write(reference(text("x")), empty()));
        }
    }
}
