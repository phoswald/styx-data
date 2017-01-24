package styx.data.impl;

import static styx.data.Values.number;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

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
    private boolean pretty;
    private boolean indent;
    private int indentCur;
    private int indentDelta;

    public Serializer(Writer writer, WriteOption[] options) {
        List<WriteOption> optionsList = Arrays.asList(options);
        this.writer = writer;
        this.pretty = optionsList.contains(WriteOption.PRETTY) || optionsList.contains(WriteOption.INDENT);
        this.indent = optionsList.contains(WriteOption.INDENT);
        if(indent) {
            indentCur = 0;
            indentDelta = 4;
        }
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
        writer.write('0');
        writer.write('x');
        for(int index = 0; index < value.byteCount(); index++) {
            int unsignedByte = value.byteAt(index) & 0xFF;
            writer.write(FormatUtils.getHexChar(unsignedByte / 16));
            writer.write(FormatUtils.getHexChar(unsignedByte % 16));
        }
    }

    private void write(Reference value) throws IOException {
        writer.write('<');
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
            indentCur += indentDelta;
            boolean first = true;
            Numeric nextAutoKey = number(1);
            for(Pair pair : value) {
                if(first) {
                    if(indent) {
                        indent(indentCur);
                    } else if(pretty) {
                        writer.write(' ');
                    }
                } else {
                    if(indent) {
                        indent(indentCur);
                    } else if(pretty) {
                        writer.write(',');
                        writer.write(' ');
                    } else {
                        writer.write(',');
                    }
                }
                first = false;
                if(pair.key().compareTo(nextAutoKey) != 0) {
                    if(pair.key().isComplex()) {
                        writer.write('@');
                    }
                    boolean originalIndent = indent;
                    indent = false;
                    write(pair.key());
                    indent = originalIndent;
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
            indentCur -= indentDelta;
            if(first) {
                if(pretty) {
                    writer.write(' ');
                }
            } else {
                if(indent) {
                    indent(indentCur);
                } else if(pretty) {
                    writer.write(' ');
                }
            }
            writer.write('}');
        }
    }

    private void indent(int num) throws IOException {
        writer.write('\n');
        while(num-- > 0) {
            writer.write(' ');
        }
    }
}
