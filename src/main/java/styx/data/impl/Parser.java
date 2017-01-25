package styx.data.impl;

import static styx.data.Values.binary;
import static styx.data.Values.complex;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.reference;
import static styx.data.Values.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import styx.data.Pair;
import styx.data.ParserException;
import styx.data.Reference;
import styx.data.Value;

public class Parser {

    private final Reader reader;
    private final FastStack<Block> stack = new FastStack<>(Block::new, Block::init);
    private char current;
    private char next;

    public Parser(Reader reader) throws IOException {
        this.reader = reader;
        this.current = (char) reader.read();
        this.next = (char) reader.read();
    }

    public Value parse() throws IOException {
        CollectingHandler handler = new CollectingHandler();
        parse(handler);
        return handler.collect();
    }

    private void parse(Handler handler) throws IOException {
        boolean hasKey = false;
        List<Value> values = new ArrayList<>();
        stack.push();
        int topStackSize = stack.size();
        while(true) {
            readWS();
            Value next = readSimple();
            if(next != null) {
                values.add(next);
            } else if(peek() == '@' && stack.size() > topStackSize) {
                skip();
                values.add(readComplex());
            } else if(eof() || (peek() == ',' && stack.size() > topStackSize) || peek() == '\n' || (peek() == '}' && stack.size() > topStackSize)) {
                if(!values.isEmpty()) {
                    if(!hasKey) {
                        values.add(0, number(stack.peek().nextAutoKey++));
                    }
                    for(int i = 0; i < values.size()-2; i++) {
                        handler.open(values.get(i));
                    }
                    handler.value(values.get(values.size()-2), values.get(values.size()-1));
                    for(int i = 0; i < values.size()-2; i++) {
                        handler.close();
                    }
                    stack.peek().elementCount++;
                }
                if(eof()) {
                    break;
                } else if(peek() == '}') {
                    for(int i = 0; i < stack.peek().levelCount; i++) {
                        handler.close();
                    }
                    stack.pop();
                    stack.peek().elementCount++;
                    if(topStackSize > 1 && stack.size() == topStackSize) {
                        stack.pop();
                        skip();
                        return;
                    }
                }
                hasKey = false;
                values.clear();
                skip();
            } else if(peek() == '{') {
                if(!hasKey) {
                    values.add(0, number(stack.peek().nextAutoKey++));
                }
                for(int i=0; i<values.size(); i++) {
                    handler.open(values.get(i));
                }
                stack.push().levelCount = values.size();
                hasKey = false;
                values.clear();
                skip();
            } else if(peek() == ':' && stack.size() > topStackSize && !hasKey && values.size() == 1) {
                hasKey = true;
                skip();
            } else {
                throw new ParserException("Unexpected token '" + peek() + "'.");
            }
        }
        if(stack.size() > 1 || stack.peek().elementCount == 0) {
            throw new ParserException("Unexpected EOF.");
        }
    }

    private void readWS() throws IOException {
        while(peek() == ' ' || peek() == '\t' || peek() == '\r') {
            skip();
        }
    }

    private Value readSimple() throws IOException {
        if(!eof()) {
            if(FormatUtils.isIdentifierStartChar(peek())) {
                StringBuilder sb = new StringBuilder();
                sb.append(read());
                while(FormatUtils.isIdentifierChar(peek())) {
                    sb.append(read());
                }
                return text(sb.toString());
            }
            if(peek() == '"') {
                skip();
                StringBuilder sb = new StringBuilder();
                while(!eof() && peek() != '"') {
                    if(peek() == '\\') {
                        skip();
                        if(!eof()) {
                            char character = read();
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
                        sb.append(read());
                    }
                }
                if(eof()) {
                    throw new ParserException("Invalid textual value: closing '\"' expected.");
                }
                skip();
                return text(sb.toString());
            }
            if(peek() == '0' && peekNext() == 'x') {
                skip(); skip();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                while(FormatUtils.isHexChar(peek()) && FormatUtils.isHexChar(peekNext())) {
                    bytes.write((FormatUtils.getHexDigit(read()) << 4) + FormatUtils.getHexDigit(read()));
                }
                if(FormatUtils.isHexChar(peek())) {
                    throw new ParserException("Invalid binary value: even number of digits expected.");
                }
                return binary(bytes.toByteArray());
            }
            if(FormatUtils.isDigit(peek()) || peek() == '-') {
                // TODO: support fractional numbers and exponential notation
                StringBuilder sb = new StringBuilder();
                sb.append(read());
                while(FormatUtils.isDigit(peek())) {
                    sb.append(read());
                }
                return number(Double.valueOf(sb.toString()));
            }
            if(peek() == '<') {
                // TODO: support complex reference values
                skip();
                if(peek() == '/') {
                    skip();
                } else {
                    throw new ParserException("Invalid reference: '/' expected.");
                }
                Reference reference = reference();
                if(peek() == '>') {
                    skip();
                } else {
                    while(true) {
                        Value child = readSimple();
                        if(child == null) {
                            throw new ParserException("Invalid reference: part or '>' expected.");
                        }
                        reference = reference.child(child);
                        if(peek() == '/') {
                            skip();
                            continue;
                        } else if(peek() == '>') {
                            skip();
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

    private Value readComplex() throws IOException {
        readWS();
        if(peek() != '{') {
            throw new ParserException("Invalid complex key: '{' expected.");
        }
        CollectingHandler handler = new CollectingHandler();
        parse(handler);
        return handler.collect();
    }

    private char read() throws IOException {
        char result = current;
        current = next;
        next = (char) reader.read();
        return result;
    }

    private void skip() throws IOException {
        current = next;
        next = (char) reader.read();
    }

    private boolean eof() {
        return current == 0xFFFF;
    }

    private char peek() {
        return current;
    }

    private char peekNext() {
        return next;
    }

    private static class Block {
        private int elementCount;
        private int levelCount;
        private int nextAutoKey;
        private void init() {
            elementCount = 0;
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
