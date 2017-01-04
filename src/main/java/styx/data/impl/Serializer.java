package styx.data.impl;

import styx.data.Binary;
import styx.data.Complex;
import styx.data.Numeric;
import styx.data.Pair;
import styx.data.Reference;
import styx.data.Text;
import styx.data.Value;

class Serializer {

    String serialize(Value value) {
        StringBuilder sb = new StringBuilder();
        serialize(sb, value);
        return sb.toString();
    }

    private void serialize(StringBuilder sb, Value value) {
        switch(value.kind()) {
            case NUMBER:
                serialize(sb, value.asNumeric()); break;
            case TEXT:
                serialize(sb, value.asText()); break;
            case BINARY:
                serialize(sb, value.asBinary()); break;
            case REFERENCE:
                serialize(sb, value.asReference()); break;
            case COMPLEX:
                serialize(sb, value.asComplex()); break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void serialize(StringBuilder sb, Numeric value) {
        sb.append(value.toDecimalString());
    }

    private void serialize(StringBuilder sb, Text value) {
        sb.append('"');
        for(int index = 0; index < value.charCount(); index++) {
            char character = value.charAt(index);
            switch(character) {
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '"':  sb.append("\\\""); break;
                default:   sb.append(character); break;
            }
        }
        sb.append('"');
    }

    private void serialize(StringBuilder sb, Binary value) {
        sb.append("0x");
        for(int index = 0; index < value.byteCount(); index++) {
            int unsignedByte = value.byteAt(index) & 0xFF;
            sb.append(getHexChar(unsignedByte / 16));
            sb.append(getHexChar(unsignedByte % 16));
        }
    }

    private void serialize(StringBuilder sb, Reference value) {
        sb.append('[');
        for(int index = 0; index < value.partCount(); index++) {
            if(index > 0) {
                sb.append(',');
            }
            sb.append(value.partAt(index).toString());
        }
        sb.append(']');
    }

    private void serialize(StringBuilder sb, Complex value) {
        sb.append('{');
        boolean first = true;
        for(Pair pair : value) {
            if(!first) {
                sb.append(',');
            }
            first = false;
            sb.append(pair.key().toString());
            sb.append(':');
            sb.append(pair.value().toString());
        }
        sb.append('}');
    }

    private static char getHexChar(int digit) {
        if(digit < 10) {
            return (char) ('0' + digit);
        } else {
            return (char) ('A' + digit - 10);
        }
    }
}
