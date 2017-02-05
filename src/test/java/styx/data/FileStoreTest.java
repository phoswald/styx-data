package styx.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.list;
import static styx.data.Values.root;
import static styx.data.Values.text;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class FileStoreTest extends GenericStoreTest {

    private static final Path file = Paths.get("target/test/FileStoreTest/store.styx");
    private static final String url = "file:" + file;

    public FileStoreTest() {
        super(url);
    }

    @Before
    public void deleteFile() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    public void testOpenFile() {
        assertFalse(Files.exists(file));
        try(Store store = Store.open(url)) {
            store.write(root(), list(text("hello")));
        }
        assertTrue(Files.exists(file));
        try(Store store = Store.open(url)) {
            store.write(root(), null);
        }
        assertFalse(Files.exists(file));
        try(Store store = Store.open(url)) {
            assertException(UncheckedIOException.class, "Failed to aquire lock for " + file, () -> Store.open(url));
            store.write(root(), list(text("hello, world")));
        }
        assertTrue(Files.exists(file));
    }
}
