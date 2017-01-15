package styx.data.impl;

import static styx.data.Values.binary;
import static styx.data.Values.complex;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.reference;
import static styx.data.Values.text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import styx.data.Pair;
import styx.data.ParserException;
import styx.data.Reference;
import styx.data.Value;

public class Parser {

    private final char[] input;
    private int pos;

    public Parser(String input) {
        this.input = input.toCharArray();
        this.pos = 0;
    }

    public Value parse() {
        CollectingHandler handler = new CollectingHandler();
        parse(handler);
        return handler.collect();
    }

    public void parse(Handler handler) {
        boolean hasKey = false;
        boolean hasStuff = false;
        List<Value> values = new ArrayList<>();
        FastStack<Block> blocks = new FastStack<>(Block::new, Block::init);
        blocks.push();
        while(true) {
            readWS();
            Value next = readSimple();
            if(next != null) {
                hasStuff = true;
                values.add(next);
            } else if(pos == input.length || input[pos] == ',' || input[pos] == '}') {
                if(!values.isEmpty()) {
                    if(!hasKey) {
                        values.add(0, number(blocks.peek().nextAutoKey++));
                    }
                    for(int i = 0; i < values.size()-2; i++) {
                        handler.open(values.get(i));
                    }
                    handler.value(values.get(values.size()-2), values.get(values.size()-1));
                    for(int i = 0; i < values.size()-2; i++) {
                        handler.close();
                    }
                }
                if(pos == input.length) {
                    break;
                } else if(input[pos] == '}') {
                    for(int i = 0; i < blocks.peek().levelCount; i++) {
                        handler.close();
                    }
                    blocks.pop();
                }
                hasKey = false;
                hasStuff = true;
                values.clear();
                pos++;
            } else if(input[pos] == '{') {
                if(!hasKey) {
                    values.add(0, number(blocks.peek().nextAutoKey++));
                }
                for(int i=0; i<values.size(); i++) {
                    handler.open(values.get(i));
                }
                blocks.push().levelCount = values.size();
                hasKey = false;
                hasStuff = false;
                values.clear();
                pos++;
            } else if(input[pos] == ':' && blocks.size() > 1 && !hasKey && values.size() == 1) {
                hasKey = true;
                pos++;
            } else {
                throw new ParserException("Unexpected token '" + input[pos] + "'.");
            }
        }
        if(!hasStuff || blocks.size() > 1) {
            throw new ParserException("Unexpected EOF.");
        }
    }

    private void readWS() {
        while(pos < input.length && input[pos] == ' ') {
            pos++;
        }
    }

//    private Value readAny() {
//        readWS();
//        Value value = readSimple();
//        if(value == null) {
//            value = readComplex();
//        }
//        if(value != null) {
//            readWS();
//            Value value2 = readAny();
//            if(value2 != null) {
//                value = complex(value, value2);
//            }
//        }
//        return value;
//    }

    private Value readSimple() {
        if(pos < input.length) {
            if(FormatUtils.isIdentifierStartChar(input[pos])) {
                StringBuilder sb = new StringBuilder();
                sb.append(input[pos++]);
                while(pos < input.length && FormatUtils.isIdentifierChar(input[pos])) {
                    sb.append(input[pos++]);
                }
                return text(sb.toString());
            }
            if(input[pos] == '"') {
                pos++;
                StringBuilder sb = new StringBuilder();
                while(pos < input.length && input[pos] != '"') {
                    if(input[pos] == '\\') {
                        pos++;
                        if(pos < input.length) {
                            char character = input[pos++];
                            switch(character) {
                                case 't':  sb.append('\t'); break;
                                case 'r':  sb.append('\r'); break;
                                case 'n':  sb.append('\n'); break;
                                case '"':  sb.append('\"'); break;
                                case '\\': sb.append('\\'); break;
                                default:
                                    throw new ParserException("Invalid textual value: invalid escape sequence '\\" + character + "'.");
                            }
                        }
                    } else {
                        sb.append(input[pos++]);
                    }
                }
                if(pos == input.length) {
                    throw new ParserException("Invalid textual value: closing '\"' expected.");
                }
                pos++;
                return text(sb.toString());
            }
            if(pos+1 < input.length && input[pos] == '0' && input[pos+1] == 'x') {
                pos+=2;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                while(pos+1 < input.length && FormatUtils.isHexChar(input[pos]) && FormatUtils.isHexChar(input[pos+1])) {
                    bytes.write((FormatUtils.getHexDigit(input[pos]) << 4) + FormatUtils.getHexDigit(input[pos+1]));
                    pos+=2;
                }
                if(pos < input.length && FormatUtils.isHexChar(input[pos])) {
                    throw new ParserException("Invalid binary value: even number of digits expected.");
                }
                return binary(bytes.toByteArray());
            }
            if(FormatUtils.isDigit(input[pos]) || input[pos] == '-') {
                // TODO: support fractional numbers and exponential notation
                StringBuilder sb = new StringBuilder();
                sb.append(input[pos++]);
                while(pos < input.length && FormatUtils.isDigit(input[pos])) {
                    sb.append(input[pos++]);
                }
                return number(Double.valueOf(sb.toString()));
            }
            if(input[pos] == '<') {
                // TODO: support complex reference values
                pos++;
                if(pos < input.length && input[pos] == '/') {
                    pos++;
                } else {
                    throw new ParserException("Invalid reference: '/' expected.");
                }
                Reference reference = reference();
                if(pos < input.length && input[pos] == '>') {
                    pos++;
                } else {
                    while(true) {
                        Value child = readSimple();
                        if(child == null) {
                            throw new ParserException("Invalid reference: part or '>' expected.");
                        }
                        reference = reference.child(child);
                        if(pos < input.length && input[pos] == '/') {
                            pos++;
                            continue;
                        } else if(pos < input.length && input[pos] == '>') {
                            pos++;
                            break;
                        } else {
                            throw new ParserException("Invalid reference: '/' or '>' expected.");
                        }
                    }
                }
                return reference;
            }
        }
        return null;
    }

//    private Value readComplex() {
//        if(pos < input.length && input[pos] == '{') {
//            List<Pair> pairs = new ArrayList<>();
//            long nextIntegerKey = 1;
//            do  {
//                pos++;
//                readWS();
//                Value value1 = readAny();
//                if(value1 != null) {
//                    readWS();
//                    if(pos < input.length && input[pos] == ':') {
//                        pos++;
//                        readWS();
//                        Value value2 = readAny();
//                        if(value2 != null) {
//                            readWS();
//                            pairs.add(pair(value1, value2));
//                        } else {
//                            throw new IllegalArgumentException();
//                        }
//                    } else {
//                        pairs.add(pair(number(nextIntegerKey++), value1));
//                    }
//                } else {
//                    break;
//                }
//                readWS();
//            } while(pos < input.length && input[pos] == ',');
//            if(pos < input.length && input[pos] == '}') {
//                pos++;
//                return complex(pairs);
//            }
//            throw new IllegalArgumentException();
//        }
//        return null;
//    }

    private static class Block {
        private int levelCount;
        private int nextAutoKey;
        private void init() {
            levelCount = 0;
            nextAutoKey = 1;
        }
    }

    private static interface Handler {
        public void open(Value key);
        public void value(Value key, Value value);
        public void close();
    }

    private static class CollectingHandler implements Handler {
        private final FastStack<Context> context = new FastStack<>(Context::new, Context::init);

        private CollectingHandler() {
            context.push();
        }

        @Override
        public void open(Value key) {
            context.push().key = key;
        }

        @Override
        public void value(Value key, Value value) {
            context.peek().pairs.add(pair(key, value));
        }

        @Override
        public void close() {
            Pair pair = pair(context.peek().key, complex(context.peek().pairs));
            context.pop();
            context.peek().pairs.add(pair);
        }

        public Value collect() {
            return context.peek().pairs.get(0).value();
        }

        private static class Context {
            private Value key;
            private List<Pair> pairs;
            private void init() {
                key = null;
                pairs = new ArrayList<>();
            }
        }
    }
}
