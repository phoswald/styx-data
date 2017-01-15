package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.binary;
import static styx.data.Values.complex;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.reference;
import static styx.data.Values.text;

import org.junit.Test;

public class ValueTest {

    @Test
    public void instanceOf_number_success() {
        Value value = number(1);
        assertTrue(value.isNumeric());
        assertFalse(value.isText());
        assertFalse(value.isBinary());
        assertFalse(value.isReference());
        assertFalse(value.isComplex());
    }

    @Test
    public void instanceOf_text_success() {
        Value value = text();
        assertFalse(value.isNumeric());
        assertTrue(value.isText());
        assertFalse(value.isBinary());
        assertFalse(value.isReference());
        assertFalse(value.isComplex());
    }

    @Test
    public void instanceOf_binary_success() {
        Value value = binary();
        assertFalse(value.isNumeric());
        assertFalse(value.isText());
        assertTrue(value.isBinary());
        assertFalse(value.isReference());
        assertFalse(value.isComplex());
    }

    @Test
    public void instanceOf_reference_success() {
        Value value = reference();
        assertFalse(value.isNumeric());
        assertFalse(value.isText());
        assertFalse(value.isBinary());
        assertTrue(value.isReference());
        assertFalse(value.isComplex());
    }

    @Test
    public void instanceOf_complex_success() {
        Value value = complex();
        assertFalse(value.isNumeric());
        assertFalse(value.isText());
        assertFalse(value.isBinary());
        assertFalse(value.isReference());
        assertTrue(value.isComplex());
    }

    @Test
    public void cast_number_success() {
        Value value = number(1);
        assertSame(value, value.asNumeric());
        assertException(ClassCastException.class, value::asText);
        assertException(ClassCastException.class, value::asBinary);
        assertException(ClassCastException.class, value::asReference);
        assertException(ClassCastException.class, value::asComplex);
    }

    @Test
    public void cast_text_success() {
        Value value = text();
        assertException(ClassCastException.class, value::asNumeric);
        assertSame(value, value.asText());
        assertException(ClassCastException.class, value::asBinary);
        assertException(ClassCastException.class, value::asReference);
        assertException(ClassCastException.class, value::asComplex);
    }

    @Test
    public void cast_binary_success() {
        Value value = binary();
        assertException(ClassCastException.class, value::asNumeric);
        assertException(ClassCastException.class, value::asText);
        assertSame(value, value.asBinary());
        assertException(ClassCastException.class, value::asReference);
        assertException(ClassCastException.class, value::asComplex);
    }

    @Test
    public void cast_reference_success() {
        Value value = reference();
        assertException(ClassCastException.class, value::asNumeric);
        assertException(ClassCastException.class, value::asText);
        assertException(ClassCastException.class, value::asBinary);
        assertSame(value, value.asReference());
        assertException(ClassCastException.class, value::asComplex);
    }

    @Test
    public void cast_complex_success() {
        Value value = complex();
        assertException(ClassCastException.class, value::asNumeric);
        assertException(ClassCastException.class, value::asText);
        assertException(ClassCastException.class, value::asBinary);
        assertException(ClassCastException.class, value::asReference);
        assertSame(value, value.asComplex());
    }

    @Test
    public void toString_number_success() {
        assertEquals("1", number(1).toString());
        assertEquals("1.25", number(1.25).toString());
    }

    @Test
    public void toString_text_success() {
        assertEquals("\"\"", text().toString());
        assertEquals("\":-)\"", text(":-)").toString());
        assertEquals("\"two words\"", text("two words").toString());
        assertEquals("\"1word\"", text("1word").toString());
        assertEquals("\"\\t\\r\\n\\\"\"", text("\t\r\n\"").toString());
        assertEquals("Ident1", text("Ident1").toString());
        assertEquals("_ident", text("_ident").toString());
    }

    @Test
    public void toString_binary_success() {
        assertEquals("0x", binary().toString());
        assertEquals("0x001234DEAD", binary(new byte[] { 0, 0x12, 0x34, (byte) 0xDE, (byte) 0xAD }).toString());
    }

    @Test
    public void toString_refernce_success() {
        assertEquals("</>", reference().toString());
        assertEquals("</part1/part2>", reference(text("part1"), text("part2")).toString());
    }

    @Test
    public void toString_complex_success() {
        assertEquals("{}", complex().toString());
        assertEquals("{key1:val1,key2:val2}", complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2"))).toString());
        assertEquals("{val1,val2}", list(text("val1"), text("val2")).toString());
        assertEquals("{3:val3,val4,key1:val1}", complex(pair(text("key1"), text("val1")), pair(number(3), text("val3")), pair(number(4), text("val4"))).toString());
        assertEquals("{1.1:val1,1.2:val2}", complex(pair(number(1.1), text("val1")), pair(number(1.2), text("val2"))).toString());
    }

    @Test
    public void equals_valid_success() {
        assertTrue(number(1).equals(number(1)));
        assertFalse(number(1).equals(number(2)));
        assertFalse(number(1).equals(null));
    }

    @Test
    public void hashCode_valid_success() {
        assertEquals("1".hashCode(), number(1).hashCode());
        assertEquals("\":-)\"".hashCode(), text(":-)").hashCode());
        assertEquals("0x001234DEAD".hashCode(), binary(new byte[] { 0, 0x12, 0x34, (byte) 0xDE, (byte) 0xAD }).hashCode());
        assertEquals("</part>".hashCode(), reference(text("part")).hashCode());
        assertEquals("{val}".hashCode(), list(text("val")).hashCode());
    }

    @Test
    public void compareTo_allCombinations_success() {
        Value[] values = new Value[] {
                number(Long.MIN_VALUE),
                number(Integer.MIN_VALUE),
                number(-1),
                number(1),
                number(1.1),
                number(2),
                number(Integer.MAX_VALUE),
                number(Long.MAX_VALUE),
                text(),
                text("A"),
                text("AA"),
                text("B"),
                binary(),
                binary(new byte[] { 1 }),
                binary(new byte[] { 1, 0 }),
                binary(new byte[] { 2 }),
                binary(new byte[] { (byte) 0xFF }), // must be compared as unsigned
                reference(),
                reference(number(1)),
                reference(text("A")),
                reference(text("A"), text("A")),
                reference(text("B")),
                complex(),
                list(text("A")), // numeric keys sort before texual keys
                list(text("A"), text("A")),
                complex(text("key"), text("val")),
                complex(text("key2"), text("val")), // compare keys first (key2 > key)
                complex(text("key2"), text("val2")), // compare values last (val2 > val)
        };
        for(int i = 0; i < values.length; i++) {
            for(int j = 0; j < values.length; j++) {
                int expectedOrder = Integer.compare(i, j);
                int actualOrder = normalizeOrder(values[i].compareTo(values[j]));
                assertEquals("Error comparing " + values[i] + " <=> " + values[j], expectedOrder, actualOrder);
            }
        }
    }

    private static int normalizeOrder(int order) {
        return Integer.compare(order, 0); // returns -1, 0, +1
    }
}
