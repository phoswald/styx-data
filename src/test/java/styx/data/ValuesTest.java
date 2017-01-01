package styx.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ValuesTest {

    @Test
    public void numberOfLong_integer_success() {
        Numeric number = Values.number(1234);
        assertNotNull(number);
        assertEquals(1234, number.toInteger());
        assertEquals(1234, number.toLong());
        assertEquals(1234.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfLong_integerMin_success() {
        Numeric number = Values.number(Integer.MIN_VALUE);
        assertNotNull(number);
        assertEquals(-2147483648, number.toInteger());
        assertEquals(-2147483648, number.toLong());
        assertEquals(-2147483648.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfLong_integerMax_success() {
        Numeric number = Values.number(Integer.MAX_VALUE);
        assertNotNull(number);
        assertEquals(2147483647, number.toInteger());
        assertEquals(2147483647, number.toLong());
        assertEquals(2147483647.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfLong_long_success() {
        Numeric number = Values.number(12340000000L);
        assertNotNull(number);
        assertException(ArithmeticException.class, () -> number.toInteger());
        assertEquals(12340000000L, number.toLong());
        assertEquals(12340000000.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfLong_longMin_success() {
        Numeric number = Values.number(Long.MIN_VALUE);
        assertNotNull(number);
        assertException(ArithmeticException.class, () -> number.toInteger());
        assertEquals(-9223372036854775808L, number.toLong());
        assertEquals(-9223372036854775808.0, number.toDouble(), 0.0); // TODO: is long really exactly represented by double?
    }

    @Test
    public void numberOfLong_longMax_success() {
        Numeric number = Values.number(Long.MAX_VALUE);
        assertNotNull(number);
        assertException(ArithmeticException.class, () -> number.toInteger());
        assertEquals(9223372036854775807L, number.toLong());
        assertEquals(9223372036854775807.0, number.toDouble(), 0.0); // TODO: is long really exactly represented by double?
    }

    @Test
    public void numberOfDouble_integer_success() {
        Numeric number = Values.number(1234.0);
        assertNotNull(number);
        assertEquals(1234, number.toInteger());
        assertEquals(1234, number.toLong());
        assertEquals(1234.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfDouble_long_success() {
        Numeric number = Values.number(12340000000.0);
        assertNotNull(number);
        assertException(ArithmeticException.class, () -> number.toInteger());
        assertEquals(12340000000L, number.toLong());
        assertEquals(12340000000.0, number.toDouble(), 0.0);
    }

    @Test
    public void numberOfDouble_double_success() {
        Numeric number = Values.number(12.34);
        assertNotNull(number);
        assertException(ArithmeticException.class, () -> number.toInteger());
        assertException(ArithmeticException.class, () -> number.toLong());
        assertEquals(12.34, number.toDouble(), 0.0);
    }

    @Test
    public void text_string_success() {
        Text text = Values.text("test");
        assertNotNull(text);
        assertEquals(4, text.charCount());
        assertEquals('e', text.charAt(1));
        assertException(IndexOutOfBoundsException.class, () -> text.charAt(5));
        assertEquals("test", text.toCharString());
        assertArrayEquals(new char[] { 't', 'e', 's', 't' }, text.toCharArray());
    }

    @Test
    public void text_empty_success() {
        Text text = Values.text("");
        assertNotNull(text);
        assertSame(text, Values.text(""));
    }

    @Test
    public void text_null_success() {
        Text text = Values.text(null);
        assertNotNull(text);
        assertSame(text, Values.text(""));
        assertEquals("", text.toCharString());
    }

    @Test
    public void text_modifiedAfterGet_notChanged() {
        Text text = Values.text("test");
        text.toCharArray()[1] = 'E';
        assertEquals('e', text.charAt(1));
    }

    @Test
    public void binary_bytes_success() {
        Binary binary = Values.binary(new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD });
        assertNotNull(binary);
        assertEquals(4, binary.byteCount());
        assertEquals(0x12, binary.byteAt(1));
        assertException(IndexOutOfBoundsException.class, () -> binary.byteAt(5));
        assertArrayEquals(new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD }, binary.toByteArray());
    }

    @Test
    public void binary_empty_success() {
        Binary binary = Values.binary(new byte[0]);
        assertNotNull(binary);
        assertSame(binary, Values.binary(new byte[0]));
    }

    @Test
    public void binary_null_success() {
        Binary binary = Values.binary(null);
        assertNotNull(binary);
        assertSame(binary, Values.binary(new byte[0]));
        assertArrayEquals(new byte[0], binary.toByteArray());
    }

    @Test
    public void binary_modifiedAfterNew_notChanged() {
        byte[] bytes = new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD };
        Binary binary = Values.binary(bytes);
        bytes[1] = 0x13;
        assertEquals(0x12, binary.byteAt(1));
    }

    @Test
    public void binary_modifiedAfterGet_notChanged() {
        byte[] bytes = new byte[] { 0x00, 0x12, (byte) 0xDE, (byte) 0xAD };
        Binary binary = Values.binary(bytes);
        binary.toByteArray()[1] = 0x13;
        assertEquals(0x12, binary.byteAt(1));
    }

    @Test
    public void root_noArg_success() {
        Reference ref = Values.root();
        assertNotNull(ref);
        assertEquals(0, ref.partCount());
        assertException(IndexOutOfBoundsException.class, () -> ref.partAt(0));
        assertFalse(ref.parent().isPresent());
    }

    @Test
    public void referenceOfValues_valid_success() {
        Reference ref = Values.reference(Values.text("part1"), Values.text("part2"));
        assertNotNull(ref);
        assertEquals(2, ref.partCount());
        assertEquals("part1", ref.partAt(0).asText().toCharString());
        assertEquals("part2", ref.partAt(1).asText().toCharString());
        assertException(IndexOutOfBoundsException.class, () -> ref.partAt(-1));
        assertException(IndexOutOfBoundsException.class, () -> ref.partAt(2));
        assertTrue(ref.parent().isPresent());
        assertNotNull(ref.parent().get());
    }

    @Test
    public void referenceOfValues_empty_root() {
        assertSame(Values.root(), Values.reference());
    }

    @Test
    public void referenceOfValues_null_root() {
        assertSame(Values.root(), Values.reference((Value[]) null));
    }

    @Test
    public void referenceOfValues_invalid_exception() {
        assertException(NullPointerException.class, () -> Values.reference((Value) null));
    }

    @Test
    public void referenceOfList_valid_success() {
        Reference ref = Values.reference(Arrays.asList(Values.text("part1"), Values.text("part2")));
        assertNotNull(ref);
        assertEquals(2, ref.partCount());
        assertTrue(ref.parent().isPresent());
        assertNotNull(ref.parent().get());
    }

    @Test
    public void referenceOfList_empty_root() {
        assertSame(Values.root(), Values.reference(Arrays.asList()));
    }

    @Test
    public void referenceOfList_null_root() {
        assertSame(Values.root(), Values.reference((List<Reference>) null));
    }

    @Test
    public void referenceOfList_invalid_exception() {
        assertException(NullPointerException.class, () -> Values.reference(Arrays.asList((Value) null)));
    }

    private void assertException(Class<?> expectedException, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expectedException.getName());
        } catch(RuntimeException e) {
            assertThat(e, CoreMatchers.instanceOf(expectedException));
        }
    }
}
