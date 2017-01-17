package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.list;
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
        String expected = "{tag:{value1,value2}}";
        Value value = complex(text("tag"), list(text("value1"), text("value2")));
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
}
