package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import styx.data.exception.InvalidWriteException;

public class StoreTest {

    @Test
    public void testOpenMemory() {
        try(
                Store unnamed1 = Store.memory();
                Store unnamed2 = Store.memory();
                Store namedA1 = Store.memory("A");
                Store namedA2 = Store.memory("A");
                Store namedB = Store.memory("B")) {
            assertNotNull(unnamed1);
            assertNotNull(unnamed2);
            assertNotNull(namedA1);
            assertNotNull(namedA2);
            assertNotNull(namedB);
            assertNotSame(unnamed1, unnamed2);
            assertSame(namedA1, namedA2);
            assertNotSame(namedA1, namedB);
        }
    }

    @Test
    public void testOpenFile() {
        Path file = Paths.get("target/test/StoreTest/1.styx");
        try(Store store = Store.file(file)) {
            store.write(root(), list(text("hello")));
        }
        assertTrue(Files.exists(file));
        try(Store store = Store.file(file)) {
            store.write(root(), null);
        }
        assertFalse(Files.exists(file));
        try(Store store = Store.file(file)) {
            assertException(UncheckedIOException.class, "Failed to aquire lock for " + file, () -> Store.file(file));
            store.write(root(), list(text("hello, world")));
        }
        assertTrue(Files.exists(file));
    }

    @Test
    public void testRead() {
        try(Store store = Store.memory()) {
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
    public void testWrite1() {
        try(Store store = Store.memory()) {
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
    public void testWrite2() {
        try(Store store = Store.memory()) {
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
    public void testWrite3() {
        try(Store store = Store.memory()) {
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
    public void testWriteSub1() {
        try(Store store = Store.memory()) {
            store.write(root(), list(text("val1"), text("val2"), text("val3")));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), null));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), empty()));
        }
    }

    @Test
    public void testWriteSub2() {
        try(Store store = Store.memory()) {
            store.write(root(), text("xxx"));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-existing value.",
                    () -> store.write(reference(text("x"), text("y"), text("z")), null));

            assertException(InvalidWriteException.class, "Attempt to write a child of a non-complex value.",
                    () -> store.write(reference(text("x")), empty()));
        }
    }
}
