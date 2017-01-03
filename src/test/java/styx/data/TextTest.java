package styx.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.text;

import org.junit.Test;

public class TextTest {

    @Test
    public void text_string_success() {
        Text value = text("test");
        assertNotNull(value);
        assertEquals(4, value.charCount());
        assertEquals('e', value.charAt(1));
        assertException(IndexOutOfBoundsException.class, () -> value.charAt(5));
        assertEquals("test", value.toCharString());
        assertArrayEquals(new char[] { 't', 'e', 's', 't' }, value.toCharArray());
    }

    @Test
    public void text_empty_success() {
        Text value = text("");
        assertNotNull(value);
        assertSame(value, text(""));
        assertEquals("", value.toCharString());
    }

    @Test
    public void text_null_success() {
        Text value = text(null);
        assertNotNull(value);
        assertSame(value, text(""));
        assertEquals("", value.toCharString());
    }

    @Test
    public void text_noArg_success() {
        Text value = text();
        assertNotNull(value);
        assertSame(value, text(""));
        assertEquals("", value.toCharString());
    }

    @Test
    public void toCharArray_modified_notChanged() {
        Text value = text("test");
        value.toCharArray()[1] = 'E';
        assertEquals('e', value.charAt(1));
    }
}
