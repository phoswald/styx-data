package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.text;
import static styx.data.Values.write;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ValuesWriteTest {

    @Test
    public void write_valid_success() throws IOException {
        String expected = "{tag:{val1,val2}}";
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        Path path = Paths.get("target/test/ValuesWriteTest/valid.styx");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StringWriter writer = new StringWriter();
        Files.createDirectories(path.getParent());
        write(path, value);
        write(stream, value);
        write(writer, value);
        assertEquals(expected, String.join("", Files.readAllLines(path)));
        assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(expected, writer.toString());
    }

    @Test(expected=UncheckedIOException.class)
    public void write_unexistingPath_success() {
        write(Paths.get("target/test/Unexisting/unexisting.styx"), complex());
    }

    @Test
    public void writePretty_valid_success() throws IOException {
        String expected = "tag { val1, val2 }";
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        Path path = Paths.get("target/test/ValuesWriteTest/valid.styx");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StringWriter writer = new StringWriter();
        Files.createDirectories(path.getParent());
        write(path, value, WriteOption.PRETTY);
        write(stream, value, WriteOption.PRETTY);
        write(writer, value, WriteOption.PRETTY);
        assertEquals(expected, String.join("", Files.readAllLines(path)));
        assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(expected, writer.toString());
    }

    @Test
    public void writePretty_empty_success() throws IOException {
        StringWriter writer = new StringWriter();
        write(writer, complex(), WriteOption.PRETTY);
        assertEquals("{ }", writer.toString());
    }

    @Test
    public void writePretty_map_success() throws IOException {
        StringWriter writer = new StringWriter();
        write(writer, complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2"))), WriteOption.PRETTY);
        assertEquals("{ key1: val1, key2: val2 }", writer.toString());
    }

    @Test
    public void writePretty_list_success() throws IOException {
        StringWriter writer = new StringWriter();
        write(writer, complex(pair(number(1), text("val1")), pair(number(2), text("val2"))), WriteOption.PRETTY);
        assertEquals("{ val1, val2 }", writer.toString());
    }

    @Test
    public void writePretty_tag_success() throws IOException {
        StringWriter writer = new StringWriter();
        write(writer, complex(text("tag"), list(text("val1"), text("val2"))), WriteOption.PRETTY);
        assertEquals("tag { val1, val2 }", writer.toString());
    }
}
