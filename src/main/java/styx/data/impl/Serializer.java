package styx.data.impl;

import static styx.data.Values.number;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import styx.data.Binary;
import styx.data.Complex;
import styx.data.Numeric;
import styx.data.Pair;
import styx.data.Reference;
import styx.data.Text;
import styx.data.Value;
import styx.data.WriteOption;

public class Serializer {

    private final Writer writer;
    private final boolean pretty;

    public Serializer(Writer writer, WriteOption[] options) {
        this.writer = writer;
        this.pretty = Arrays.asList(options).contains(WriteOption.PRETTY);
    }

    public void serialize(Value value) throws IOException {
        write(value);
    }

    private void write(Value value) throws IOException {
        switch(value.kind()) {
            case NUMBER:
                write(value.asNumeric()); break;
            case TEXT:
                write(value.asText()); break;
            case BINARY:
                write(value.asBinary()); break;
            case REFERENCE:
                write(value.asReference()); break;
            case COMPLEX:
                write(value.asComplex()); break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void write(Numeric value) throws IOException {
        writer.write(value.toDecimalString());
    }

    private void write(Text value) throws IOException {
        if(FormatUtils.isIdentifier(value)) {
            writer.write(value.toCharString());
        } else {
            writer.write('"');
            for(int index = 0; index < value.charCount(); index++) {
                char character = value.charAt(index);
                switch(character) {
                    case '\t': writer.write("\\t"); break;
                    case '\r': writer.write("\\r"); break;
                    case '\n': writer.write("\\n"); break;
                    case '"':  writer.write("\\\""); break;
                    case '\\': writer.write("\\\\"); break;
                    default:   writer.write(character); break;
                }
            }
            writer.write('"');
        }
    }

    private void write(Binary value) throws IOException {
        writer.write("0x");
        for(int index = 0; index < value.byteCount(); index++) {
            int unsignedByte = value.byteAt(index) & 0xFF;
            writer.write(FormatUtils.getHexChar(unsignedByte / 16));
            writer.write(FormatUtils.getHexChar(unsignedByte % 16));
        }
    }

    private void write(Reference value) throws IOException {
        writer.write("<");
        if(!value.parent().isPresent()) {
            writer.write('/');
        }
        for(int index = 0; index < value.partCount(); index++) {
            writer.write('/');
            write(value.partAt(index));
        }
        writer.write('>');
    }

    private void write(Complex value) throws IOException {
        if(pretty && FormatUtils.isTag(value)) {
            Pair pair = value.iterator().next();
            write(pair.key());
            writer.write(' ');
            write(pair.value());
        } else {
            writer.write('{');
            if(pretty) {
                writer.write(' ');
            }
            boolean first = true;
            Numeric nextAutoKey = number(1);
            for(Pair pair : value) {
                if(!first) {
                    writer.write(',');
                    if(pretty) {
                        writer.write(' ');
                    }
                }
                first = false;
                if(pair.key().compareTo(nextAutoKey) != 0) {
                    if(pair.key().isComplex()) {
                        writer.write('@');
                    }
                    write(pair.key());
                    writer.write(':');
                    if(pretty) {
                        writer.write(' ');
                    }
                }
                if(pair.key().isNumeric() && pair.key().asNumeric().isInteger()) {
                    nextAutoKey = number(pair.key().asNumeric().toInteger() + 1);
                }
                write(pair.value());
            }
            if(pretty && !first) {
                writer.write(' ');
            }
            writer.write('}');
        }
    }
}
