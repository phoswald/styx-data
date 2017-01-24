package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.complex;
import static styx.data.Values.generate;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class GeneratorTest {

    @Test
    public void generate_valid_success() throws IOException {
        String expected = "{tag:{val1,val2}}";
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        Path path = Paths.get("target/test/ValuesWriteTest/valid.styx");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StringWriter writer = new StringWriter();
        Files.createDirectories(path.getParent());
        generate(value, path);
        generate(value, stream);
        generate(value, writer);
        assertEquals(expected, generate(value));
        assertEquals(expected, String.join("", Files.readAllLines(path)));
        assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(expected, writer.toString());
    }

    @Test
    public void generate_validPretty_success() throws IOException {
        String expected = "tag { val1, val2 }";
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        Path path = Paths.get("target/test/ValuesWriteTest/valid.styx");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StringWriter writer = new StringWriter();
        Files.createDirectories(path.getParent());
        generate(value, path, GeneratorOption.PRETTY);
        generate(value, stream, GeneratorOption.PRETTY);
        generate(value, writer, GeneratorOption.PRETTY);
        assertEquals(expected, generate(value, GeneratorOption.PRETTY));
        assertEquals(expected, String.join("", Files.readAllLines(path)));
        assertEquals(expected, new String(stream.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(expected, writer.toString());
    }

    @Test(expected=UncheckedIOException.class)
    public void generate_unexistingPath_success() {
        generate(complex(), Paths.get("target/test/Unexisting/unexisting.styx"));
    }

    @Test
    public void generate_complexEmpty_success() throws IOException {
        Value value = complex();
        assertEquals("{}", generate(value));
        assertEquals("{ }", generate(value, GeneratorOption.PRETTY));
        assertEquals("{ }", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexMap_success() throws IOException {
        Value value = complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2")));
        assertEquals("{key1:val1,key2:val2}", generate(value));
        assertEquals("{ key1: val1, key2: val2 }", generate(value, GeneratorOption.PRETTY));
        assertEquals("{\n    key1: val1\n    key2: val2\n}", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexList1_success() throws IOException {
        Value value = list(text("val1"));
        assertEquals("{val1}", generate(value));
        assertEquals("{ val1 }", generate(value, GeneratorOption.PRETTY));
        assertEquals("{\n    val1\n}", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexList2_success() throws IOException {
        Value value = list(text("val1"), text("val2"));
        assertEquals("{val1,val2}", generate(value));
        assertEquals("{ val1, val2 }", generate(value, GeneratorOption.PRETTY));
        assertEquals("{\n    val1\n    val2\n}", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexTag_success() throws IOException {
        Value value = complex(text("tag"), list(text("val1"), text("val2")));
        assertEquals("{tag:{val1,val2}}", generate(value));
        assertEquals("tag { val1, val2 }", generate(value, GeneratorOption.PRETTY));
        assertEquals("tag {\n    val1\n    val2\n}", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexNested_success() throws IOException {
        Value value = complex(text("tag"), list(list(text("val1")), list(text("val2"), text("val3")), complex(text("tag4"), list(text("val4")))));
        assertEquals("{tag:{{val1},{val2,val3},{tag4:{val4}}}}", generate(value));
        assertEquals("tag { { val1 }, { val2, val3 }, tag4 { val4 } }", generate(value, GeneratorOption.PRETTY));
        assertEquals("tag {\n    {\n        val1\n    }\n    {\n        val2\n        val3\n    }\n    tag4 {\n        val4\n    }\n}", generate(value, GeneratorOption.INDENT));
    }

    @Test
    public void generate_complexComplexKey_success() throws IOException {
        Value value = complex(complex(pair(text("key1"), number(1)), pair(text("key2"), number(2))), list(text("val1"), text("val2")));
        assertEquals("{@{key1:1,key2:2}:{val1,val2}}", generate(value));
        assertEquals("{ @{ key1: 1, key2: 2 }: { val1, val2 } }", generate(value, GeneratorOption.PRETTY));
        assertEquals("{\n    @{ key1: 1, key2: 2 }: {\n        val1\n        val2\n    }\n}", generate(value, GeneratorOption.INDENT));
    }
}
