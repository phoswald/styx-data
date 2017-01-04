package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.pair;
import static styx.data.Values.text;

import org.junit.Test;

public class PairTest {

    @Test
    public void pair_valid_success() {
        Pair pair = pair(text("k"), text("v"));
        assertNotNull(pair);
        assertEquals(text("k"), pair.key());
        assertEquals(text("v"), pair.value());
    }

    @Test
    public void pair_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> pair(text("k"), null));
        assertException(IllegalArgumentException.class, () -> pair(null, text("v")));
    }
}
