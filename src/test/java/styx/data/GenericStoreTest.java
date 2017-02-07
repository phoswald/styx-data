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

import org.junit.Before;
import org.junit.Test;

import styx.data.exception.InvalidAccessException;

public abstract class GenericStoreTest {

    protected final String url;

    protected GenericStoreTest(String url) {
        this.url = url;
    }

    @Before
    public void prepare() {
        try(Store store = Store.open(url)) {
            store.write(root(), null);
        }
    }

    @Test
    public void readWrite_valid_success() {
        try(Store store = Store.open(url)) {
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
    public void readWrite_valid2_success() {
        try(Store store = Store.open(url)) {
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
    public void readWrite_valid3_success() {
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
    public void readWrite_valid4_success() {
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
    public void write_nonExisting_exception() {
        try(Store store = Store.open(url)) {
            store.write(root(), complex(text("key"), text("val")));

            assertException(InvalidAccessException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("validKey"), text("badKey")), null));

            assertException(InvalidAccessException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("validKey"), text("badKey")), empty()));
        }
    }

    @Test
    public void write_nonComplex_exception() {
        try(Store store = Store.open(url)) {
            store.write(root(), complex(text("key"), text("val")));

            assertException(InvalidAccessException.class, "Attempt to write a child of a non-complex value.",
                    () -> store.write(reference(text("key"), text("badKey")), null));

            assertException(InvalidAccessException.class, "Attempt to write a child of a non-complex value.",
                    () -> store.write(reference(text("key"), text("badKey")), empty()));
        }
    }

    @Test
    public void browse_flatComplex_valid() {
        try(Store store = Store.open(url)) {
            Value value = list(text("val1"), text("val2"), empty(), empty());
            store.write(root(), value);

            Pair[] pairs = store.browse(root()).toArray(Pair[]::new);

            assertEquals(value, complex(pairs));
        }
    }

    @Test
    public void browse_deepComplex_valid() {
        try(Store store = Store.open(url)) {
            Value value = complex(
                    pair(text("A"), complex(text("AA"), complex(text("AAA"),
                        complex(pair(text("keyA1"), text("valueA1")), pair(text("keyA2"), text("valueA2")))))),
                    pair(text("B"), complex(text("BA"), complex(text("BAA"),
                        complex(pair(text("keyB1"), text("valueB1")), pair(text("keyB2"), text("valueB2")))))));
            store.write(root(), value);

            Pair[] pairs = store.browse(root()).toArray(Pair[]::new);

            assertEquals(
                    complex(pair(text("A"), complex()), pair(text("B"), complex())),
                    complex(pairs));
        }
    }

    @Test
    public void browse_nonComplex_exception() {
        try(Store store = Store.open(url)) {
            store.write(root(), complex(text("key"), text("val")));

            assertException(InvalidAccessException.class, "Attempt to browse children of a non-existing value.",
                    () -> store.browse(reference(text("badKey"))));

            assertException(InvalidAccessException.class, "Attempt to browse children of a non-complex value.",
                    () -> store.browse(reference(text("key"))));
        }
    }
}
