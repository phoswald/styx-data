package styx.data.impl;

import static styx.data.Values.binary;
import static styx.data.Values.number;
import static styx.data.Values.reference;
import static styx.data.Values.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

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
        boolean lineHasKey = false;
        List<Value> lineValues = new ArrayList<>();
        boolean isParsingNested = stack.size() > 0;
        Block top = stack.push();
        while(true) {
            boolean isAtTop = (stack.peek() == top);
            readWS();
            Value next = readSimple();
            if(next != null) {
                lineValues.add(next);
            } else if(peek() == '@' && !isAtTop) {
                skip();
                lineValues.add(readComplex());
            } else if(eof() || peek() == '\n' || (peek() == ',' && !isAtTop) || (peek() == '}' && !isAtTop)) {
                if(!lineValues.isEmpty()) {
                    if(!lineHasKey) {
                        lineValues.add(0, number(stack.peek().nextAutoKey++));
                    }
                    for(int i = 0; i < lineValues.size()-2; i++) {
                        handler.open(lineValues.get(i));
                    }
                    handler.value(lineValues.get(lineValues.size()-2), lineValues.get(lineValues.size()-1));
                    for(int i = 0; i < lineValues.size()-2; i++) {
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
                    if(isParsingNested && stack.peek() == top) {
                        stack.pop();
                        skip();
                        return;
                    }
                }
                lineHasKey = false;
                lineValues.clear();
                skip();
            } else if(peek() == '{') {
                if(!lineHasKey) {
                    lineValues.add(0, number(stack.peek().nextAutoKey++));
                }
                for(int i=0; i<lineValues.size(); i++) {
                    handler.open(lineValues.get(i));
                }
                stack.push().levelCount = lineValues.size();
                lineHasKey = false;
                lineValues.clear();
                skip();
            } else if(peek() == ':' && !isAtTop && !lineHasKey && lineValues.size() == 1) {
                if(lineValues.get(0).isNumeric() && lineValues.get(0).asNumeric().isInteger()) {
                    stack.peek().nextAutoKey = lineValues.get(0).asNumeric().toInteger() + 1;
                }
                lineHasKey = true;
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
                if(FormatUtils.isIdentifierChar(peek())) {
                    throw new ParserException("Invalid binary value: unexpected token '" + peek() + "'.");
                }
                return binary(bytes.toByteArray());
            }
            if(FormatUtils.isDigit(peek()) || peek() == '-') {
                StringBuilder sb = new StringBuilder();
                sb.append(read());
                while(FormatUtils.isDigit(peek())) {
                    sb.append(read());
                }
                if(peek() == '.') {
                    sb.append(read());
                    while(FormatUtils.isDigit(peek())) {
                        sb.append(read());
                    }
                }
                if(peek() == 'E') {
                    sb.append(read());
                    if(peek() == '-') {
                        sb.append(read());
                    }
                    while(FormatUtils.isDigit(peek())) {
                        sb.append(read());
                    }
                }
                if(FormatUtils.isIdentifierChar(peek()) || peek() == '-' || peek() == '.') {
                    throw new ParserException("Invalid numeric value: unexpected token '" + peek() + "'.");
                }
                return number(Double.valueOf(sb.toString()));
            }
            if(peek() == '<') {
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
                        if(child == null && peek() == '{') {
                            child = readComplex();
                        }
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
}
