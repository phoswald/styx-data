package styx.data;

import static styx.data.Values.root;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MemoryStoreTest extends GenericStoreTest {

    public MemoryStoreTest(String url) {
        super(url);
    }

    @Parameters(name="{0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.<Object[]>asList(
            new Object[] { "memory" },
            new Object[] { "memory:generic" },
            new Object[] { "memorydb" },
            new Object[] { "memorydb:generic" }
        );
    }

    @Before
    public void deleteContent() throws IOException {
        try(Store store = Store.open(url)) {
            store.write(root(), null);
        }
    }
}
