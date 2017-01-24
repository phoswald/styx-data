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
    public void write_complexEmpty_success() throws IOException {
        Value value = complex();
        assertEquals("{}", serialize(value));
        assertEquals("{ }", serialize(value, WriteOption.PRETTY));
        assertEquals("{ }", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexMap_success() throws IOException {
        Value value = complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2")));
        assertEquals("{key1:val1,key2:val2}", serialize(value));
        assertEquals("{ key1: val1, key2: val2 }", serialize(value, WriteOption.PRETTY));
        assertEquals("{\n    key1: val1\n    key2: val2\n}", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexList1_success() throws IOException {
        Value value = list(text("val1"));
        assertEquals("{val1}", serialize(value));
        assertEquals("{ val1 }", serialize(value, WriteOption.PRETTY));
        assertEquals("{\n    val1\n}", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexList2_success() throws IOException {
        Value value = list(text("val1"), text("val2"));
        assertEquals("{val1,val2}", serialize(value));
        assertEquals("{ val1, val2 }", serialize(value, WriteOption.PRETTY));
        assertEquals("{\n    val1\n    val2\n}", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexTag_success() throws IOException {
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        assertEquals("{tag:{val1,val2}}", serialize(value));
        assertEquals("tag { val1, val2 }", serialize(value, WriteOption.PRETTY));
        assertEquals("tag {\n    val1\n    val2\n}", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexNested_success() throws IOException {
        Value value = complex(text("tag"), list(list(text("val1")), list(text("val2"), text("val3")), complex(text("tag4"), list(text("val4")))));
        assertEquals("{tag:{{val1},{val2,val3},{tag4:{val4}}}}", serialize(value));
        assertEquals("tag { { val1 }, { val2, val3 }, tag4 { val4 } }", serialize(value, WriteOption.PRETTY));
        assertEquals("tag {\n    {\n        val1\n    }\n    {\n        val2\n        val3\n    }\n    tag4 {\n        val4\n    }\n}", serialize(value, WriteOption.INDENT));
    }

    @Test
    public void write_complexComplexKey_success() throws IOException {
        Value value = complex(complex(pair(text("key1"), number(1)), pair(text("key2"), number(2))), list(text("val1"), text("val2")));
        assertEquals("{@{key1:1,key2:2}:{val1,val2}}", serialize(value));
        assertEquals("{ @{ key1: 1, key2: 2 }: { val1, val2 } }", serialize(value, WriteOption.PRETTY));
        assertEquals("{\n    @{ key1: 1, key2: 2 }: {\n        val1\n        val2\n    }\n}", serialize(value, WriteOption.INDENT));
    }
}
