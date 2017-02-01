package styx.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.list;
import static styx.data.Values.root;
import static styx.data.Values.text;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class StoreFileTest {

    private final Path file = Paths.get("target/test/StoreFileTest/store.styx");

    @Before
    public void prepare() {
        try(Store store = Store.file(file)) {
            store.write(root(), null);
        }
        assertFalse(Files.exists(file));
    }

    @Test
    public void testOpenFile() {
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
}
