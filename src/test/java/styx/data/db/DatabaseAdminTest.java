package styx.data.db;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.pair;
import static styx.data.Values.root;
import static styx.data.Values.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import styx.data.Store;
import styx.data.Value;

public class DatabaseAdminTest {

    private final Path importFile = Paths.get("src/test/resources/database-import.txt");
    private final Path exportFile = Paths.get("target/test/DatabaseAdminTest/database-export.txt");
    private final Value value = complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2")));
    private final String storeUrl = "memorydb:somename";
    private final Store store = Store.open(storeUrl);
    private final DatabaseAdmin testee = new DatabaseAdmin();

    @Before
    public void prepare() {
        store.write(root(), null);
    }

    @Test
    public void testImport() throws IOException {
        testee.importDatabase(store, importFile);
        Value actual = store.read(root()).orElse(null);
        assertEquals(value, actual);
    }

    @Test
    public void testExport() throws IOException {
        store.write(root(), value);
        testee.exportDatabase(store, exportFile);
        assertEquals(readTextFile(importFile), readTextFile(exportFile));
    }

    @Test
    public void testMain() throws IOException {
        DatabaseAdmin.main(new String[] { }); // must not throw an exception!
    }

    @Test
    public void testMainImport() throws IOException {
        DatabaseAdmin.main(new String[] { "-import", storeUrl, importFile.toString() });
        Value actual = store.read(root()).orElse(null);
        assertEquals(value, actual);
    }

    @Test
    public void testMainExport() throws IOException {
        store.write(root(), value);
        DatabaseAdmin.main(new String[] { "-export", storeUrl, exportFile.toString() });
        assertEquals(readTextFile(importFile), readTextFile(exportFile));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMainExportInvalidUrl() throws IOException {
        DatabaseAdmin.main(new String[] { "-export", "memory", exportFile.toString() });
    }

    private String readTextFile(Path file) throws IOException {
        return Files.lines(file).collect(Collectors.joining("\n"));
    }
}
