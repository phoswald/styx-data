package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.hamcrest.CoreMatchers;

class AssertUtils {

    static void assertException(Class<?> expectedException, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expectedException.getName());
        } catch(RuntimeException e) {
            assertThat(e, CoreMatchers.instanceOf(expectedException));
        }
    }

    static void assertEqualPairs(List<Pair> expectedPairs, Complex actualPairs) {
        for(Pair expectedPair : expectedPairs) {
            Optional<Value> actualValue = actualPairs.get(expectedPair.key());
            assertTrue("key not found: " + expectedPair.key(), actualValue.isPresent());
            assertEquals("different values for key: " + expectedPair.key(), expectedPair.value(), actualValue.get());
        }
        assertEquals(expectedPairs.size(), actualPairs.entries().count());
    }
}
