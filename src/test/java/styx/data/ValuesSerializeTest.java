package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.serialize;
import static styx.data.Values.text;

import java.io.IOException;

import org.junit.Test;

public class ValuesSerializeTest {

    @Test
    public void writePretty_complexEmpty_success() throws IOException {
        assertEquals("{ }", serialize(complex(), WriteOption.PRETTY));
    }

    @Test
    public void writePretty_complexMap_success() throws IOException {
        assertEquals("{ key1: val1, key2: val2 }", serialize(complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2"))), WriteOption.PRETTY));
    }

    @Test
    public void writePretty_complexList1_success() throws IOException {
        assertEquals("{ val1 }", serialize(complex(pair(number(1), text("val1"))), WriteOption.PRETTY));
    }

    @Test
    public void writePretty_complexList2_success() throws IOException {
        assertEquals("{ val1, val2 }", serialize(complex(pair(number(1), text("val1")), pair(number(2), text("val2"))), WriteOption.PRETTY));
    }

    @Test
    public void writePretty_complexTag_success() throws IOException {
        assertEquals("tag { val1, val2 }", serialize(complex(text("tag"), list(text("val1"), text("val2"))), WriteOption.PRETTY));
    }
}
