package styx.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import styx.data.impl.AbstractNumeric;
import styx.data.impl.BinaryTreeComplex;
import styx.data.impl.ByteArrayBinary;
import styx.data.impl.DefaultPair;
import styx.data.impl.LinkedListReference;
import styx.data.impl.Parser;
import styx.data.impl.StringText;

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
        return read(new StringReader(input));
    }

    public static Value read(Path path) {
        return read(path, CHARSET);
    }

    public static Value read(Path path, Charset charset) {
        try {
            return read(Files.newBufferedReader(path, charset));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Value read(InputStream stream) {
        return read(stream, CHARSET);
    }

    public static Value read(InputStream stream, Charset charset) {
        return read(new BufferedReader(new InputStreamReader(stream, charset)));
    }

    public static Value read(Reader reader) {
        try {
            return new Parser(reader).parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
