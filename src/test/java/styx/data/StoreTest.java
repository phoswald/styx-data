package styx.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class StoreTest {

    @Test
    public void testOpenMemory() {
        try(
                Store unnamed1 = Store.open("memory");
                Store unnamed2 = Store.open("memory");
                Store unnamed3 = Store.open("memory:");
                Store unnamed4 = Store.open("memory:");
                Store namedA1 = Store.open("memory:A");
                Store namedA2 = Store.open("memory:A");
                Store namedB = Store.open("memory:B")) {
            assertNotNull(unnamed1);
            assertNotNull(unnamed2);
            assertNotNull(namedA1);
            assertNotNull(namedA2);
            assertNotNull(namedB);
            assertNotSame(unnamed1, unnamed2);
            assertNotSame(unnamed3, unnamed4);
            assertSame(namedA1, namedA2);
            assertNotSame(namedA1, namedB);
        }
    }
}
