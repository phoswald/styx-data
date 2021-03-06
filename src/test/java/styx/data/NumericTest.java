package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.number;

import org.junit.Test;

public class NumericTest {

    @Test
    public void number_integer_success() {
        Numeric value = number(1234);
        assertNotNull(value);
        assertTrue(value.isInteger());
        assertEquals(1234, value.toInteger());
        assertEquals(1234, value.toLong());
        assertEquals(1234.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_integerMin_success() {
        Numeric value = number(Integer.MIN_VALUE);
        assertNotNull(value);
        assertTrue(value.isInteger());
        assertEquals(-2147483648, value.toInteger());
        assertEquals(-2147483648, value.toLong());
        assertEquals(-2147483648.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_integerMax_success() {
        Numeric value = number(Integer.MAX_VALUE);
        assertNotNull(value);
        assertTrue(value.isInteger());
        assertEquals(2147483647, value.toInteger());
        assertEquals(2147483647, value.toLong());
        assertEquals(2147483647.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_long_success() {
        Numeric value = number(12340000000L);
        assertNotNull(value);
        assertFalse(value.isInteger());
        assertException(ArithmeticException.class, () -> value.toInteger());
        assertEquals(12340000000L, value.toLong());
        assertEquals(12340000000.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_longMin_success() {
        Numeric value = number(Long.MIN_VALUE);
        assertNotNull(value);
        assertFalse(value.isInteger());
        assertException(ArithmeticException.class, () -> value.toInteger());
        assertEquals(-9223372036854775808L, value.toLong());
        assertEquals(-9223372036854775808.0, value.toDouble(), 0.0); // TODO: is long really exactly represented by double?
    }

    @Test
    public void number_longMax_success() {
        Numeric value = number(Long.MAX_VALUE);
        assertNotNull(value);
        assertFalse(value.isInteger());
        assertException(ArithmeticException.class, () -> value.toInteger());
        assertEquals(9223372036854775807L, value.toLong());
        assertEquals(9223372036854775807.0, value.toDouble(), 0.0); // TODO: is long really exactly represented by double?
    }

    @Test
    public void number_intDouble_success() {
        Numeric value = number(1234.0);
        assertNotNull(value);
        assertEquals(1234, value.toInteger());
        assertEquals(1234, value.toLong());
        assertEquals(1234.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_longDouble_success() {
        Numeric value = number(12340000000.0);
        assertNotNull(value);
        assertFalse(value.isInteger());
        assertException(ArithmeticException.class, () -> value.toInteger());
        assertEquals(12340000000L, value.toLong());
        assertEquals(12340000000.0, value.toDouble(), 0.0);
    }

    @Test
    public void number_double_success() {
        Numeric value = number(12.34);
        assertNotNull(value);
        assertFalse(value.isInteger());
        assertException(ArithmeticException.class, () -> value.toInteger());
        assertException(ArithmeticException.class, () -> value.toLong());
        assertEquals(12.34, value.toDouble(), 0.0);
    }
}
