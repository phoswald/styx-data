package styx.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import styx.data.impl.Parser;
import styx.data.impl.Generator;
import styx.data.impl.value.AbstractNumeric;
import styx.data.impl.value.BinaryTreeComplex;
import styx.data.impl.value.ByteArrayBinary;
import styx.data.impl.value.DefaultPair;
import styx.data.impl.value.LinkedListReference;
import styx.data.impl.value.StringText;

public class Values {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static Numeric number(long value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Numeric number(double value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Text text() {
        return text(null);
    }

    public static Text text(String value) {
        return StringText.valueOf(value);
    }

    public static Binary binary() {
        return binary(null);
    }

    public static Binary binary(byte[] value) {
        return ByteArrayBinary.valueOf(value);
    }

    public static Reference root() {
        return LinkedListReference.ROOT;
    }

    public static Reference reference(Value... parts) {
        return root().descendant(parts);
    }

    public static Reference reference(Collection<? extends Value> parts) {
        return root().descendant(parts);
    }

    public static Complex empty() {
        return BinaryTreeComplex.EMPTY;
    }

    public static Complex complex(Value key, Value value) {
        return empty().put(key, value);
    }

    public static Complex complex(Pair... pairs) {
        return empty().putAll(pairs);
    }

    public static Complex complex(Collection<Pair> pairs) {
        return empty().putAll(pairs);
    }

    public static Complex complex(Map<? extends Value, ? extends Value> values) {
        return empty().putAll(values);
    }

    public static Complex list(Value... values) {
        return empty().addAll(values);
    }

    public static Complex list(Collection<? extends Value> values) {
        return empty().addAll(values);
    }

    public static Pair pair(Value key, Value value) {
        return new DefaultPair(key, value);
    }

    public static Value parse(String input) {
        return parse(new StringReader(input));
    }

    public static Value parse(Path path) {
        return parse(path, CHARSET);
    }

    public static Value parse(Path path, Charset charset) {
        try {
            return parse(Files.newBufferedReader(path, charset));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Value parse(InputStream stream) {
        return parse(stream, CHARSET);
    }

    public static Value parse(InputStream stream, Charset charset) {
        return parse(new BufferedReader(new InputStreamReader(stream, charset)));
    }

    public static Value parse(Reader reader) {
        try {
            return new Parser(reader).parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String generate(Value value, GeneratorOption... options) {
        StringWriter writer = new StringWriter();
        generate(value, writer, options);
        return writer.toString();
    }

    public static void generate(Value value, Path path, GeneratorOption... options) {
        generate(value, path, CHARSET, options);
    }

    public static void generate(Value value, Path path, Charset charset, GeneratorOption... options) {
        try {
            generate(value, Files.newBufferedWriter(path, charset), options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void generate(Value value, OutputStream stream, GeneratorOption... options) {
        generate(value, stream, CHARSET, options);
    }

    public static void generate(Value value, OutputStream stream, Charset charset, GeneratorOption... options) {
        generate(value, new BufferedWriter(new OutputStreamWriter(stream, charset)), options);
    }

    public static void generate(Value value, Writer writer, GeneratorOption... options) {
        try {
            new Generator(writer, options).serialize(value);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
