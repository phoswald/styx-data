package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.list;
import static styx.data.Values.read;
import static styx.data.Values.text;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.Test;

public class ValuesReadTest {

    @Test
    public void read_valid_success() {
        Value expected = complex(text("tag"), list(text("value1"), text("value2")));
        assertEquals(expected, read(Paths.get("src/test/resources/valid.styx")));
        assertEquals(expected, read(new ByteArrayInputStream("tag { value1, value2 }".getBytes(StandardCharsets.UTF_8))));
        assertEquals(expected, read(new StringReader("tag { value1, value2 }")));
    }

    @Test(expected=UncheckedIOException.class)
    public void read_unexistingPath_success() {
        read(Paths.get("src/test/resources/unexisting.styx"));
    }
}
