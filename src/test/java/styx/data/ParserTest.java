package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.Values.binary;
import static styx.data.Values.complex;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.parse;
import static styx.data.Values.reference;
import static styx.data.Values.text;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void parse_valid_success() {
        Value expected = complex(text("tag"), list(text("value1"), text("value2")));
        assertEquals(expected, parse("tag { value1, value2 }"));
        assertEquals(expected, parse(Paths.get("src/test/resources/valid.styx")));
        assertEquals(expected, parse(new ByteArrayInputStream("tag { value1, value2 }".getBytes(StandardCharsets.UTF_8))));
        assertEquals(expected, parse(new StringReader("tag { value1, value2 }")));
    }

    @Test(expected=UncheckedIOException.class)
    public void parse_unexistingPath_success() {
        parse(Paths.get("src/test/resources/unexisting.styx"));
    }

    @Test
    public void parse_binaryInteger_success() {
        assertEquals(number(0), parse("0"));
        assertEquals(number(0), parse("  0  "));
        assertEquals(number(1234), parse("  1234  "));
        assertEquals(number(-1234), parse("  -1234  "));
    }

    @Test
    public void parse_textQuoted_success() {
        assertEquals(text(""), parse("\"\""));
        assertEquals(text(""), parse("  \"\"  "));
        assertEquals(text("xxx"), parse("  \"xxx\"  "));
        assertEquals(text(":-)"), parse("  \":-)\"  "));
        assertEquals(text("\t\r\n\"\\"), parse("\"\\t\\r\\n\\\"\\\\\""));
    }

    @Test
    public void parse_textQuotedNotClosed_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid textual value: closing '\"' expected."));
        parse("\"");
    }

    @Test
    public void parse_textQuotedInvalidEsacape_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid textual value: invalid escape sequence '\\a'."));
        parse("\"\\a\"");
    }

    @Test
    public void parse_textQuotedInvalidEsacape2_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid textual value: closing '\"' expected."));
        parse("\"\\");
    }

    @Test
    public void parse_textIdentifier_success() {
        assertEquals(text("xxx"), parse("xxx"));
        assertEquals(text("xxx"), parse("  xxx  "));
        assertEquals(text("xxx"), parse("\t\r\n\txxx\t\r\n\t"));
    }

    @Test
    public void parse_binaryValid_success() {
        Value expected = binary(new byte[] { 0, 0x12, 0x34, (byte) 0xDE, (byte) 0xAD });
        assertEquals(binary(), parse("0x"));
        assertEquals(binary(), parse("  0x  "));
        assertEquals(expected, parse("0x001234DEAD"));
        assertEquals(expected, parse("  0x001234DEAD  "));
    }

    @Test
    public void parse_binaryOddDigits_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid binary value: even number of digits expected."));
        parse("  0x0  ");
    }

    @Test
    public void parse_referenceValid_success() {
        assertEquals(reference(), parse("</>"));
        assertEquals(reference(text("part1")), parse("</part1>"));
        assertEquals(reference(text("part1"), text("part2")), parse("</part1/part2>"));
    }

    @Test
    public void parse_referenceBadToken1_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: '/' expected."));
        parse("<,");
    }

    @Test
    public void parse_referenceBadToken2_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: part or '>' expected."));
        parse("</,");
    }

    @Test
    public void parse_referenceBadToken3_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: '/' or '>' expected."));
        parse("</part,");
    }

    @Test
    public void parse_referenceNotClosed1_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: '/' expected."));
        parse("<");
    }

    @Test
    public void parse_referenceNotClosed2_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: part or '>' expected."));
        parse("</");
    }

    @Test
    public void parse_referenceNotClosed3_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Invalid reference: '/' or '>' expected."));
        parse("</part");
    }

    @Test
    public void parse_complexEmpty_success() {
        assertEquals(complex(), parse("{}"));
        assertEquals(complex(), parse("  {  }  "));
        assertEquals(complex(), parse("{\r\n}"));
        assertEquals(complex(), parse("\r\n{\r\n}\r\n"));
    }

    @Test
    public void parse_complexMap_success() {
        Value expected = complex(pair(text("key1"), text("val1")), pair(text("key2"), text("val2")));
        assertEquals(expected, parse("{key1:val1,key2:val2}"));
        assertEquals(expected, parse("  { key1 : val1 , key2 : val2 }  "));
        assertEquals(expected, parse("{\r\n\tkey1: val1\r\n\tkey2: val2\r\n}"));
    }

    @Test
    public void parse_complexList_success() {
        Value expected = list(text("val1"), text("val2"));
        assertEquals(expected, parse("{val1,val2}"));
        assertEquals(expected, parse("  { val1 , val2 }  "));
        assertEquals(expected, parse("{\r\n\tval1\r\n\tval2\r\n}"));
    }

    @Test
    public void parse_complexTag1_success() {
        Value expected = complex(text("tag"), text("val"));
        assertEquals(expected, parse("tag val"));
        assertEquals(expected, parse("  tag  val  "));
        assertEquals(expected, parse("\t\r\n\ttag\tval\r\n\t"));
        assertEquals(expected, parse("{tag:val}"));
    }

    @Test
    public void parse_complexTag2_success() {
        Value expected = complex(text("tag"), list(text("val1"), text("val2")));
        assertEquals(expected, parse("tag{val1,val2}"));
        assertEquals(expected, parse("  tag { val1 , val2 }  "));
        assertEquals(expected, parse("{tag:{val1,val2}}"));
    }

    @Test
    public void parse_complexTag3_success() {
        Value expected = complex(text("tag1"), complex(text("tag2"), list(text("val1"), text("val2"))));
        assertEquals(expected, parse("tag1 tag2{val1,val2}"));
        assertEquals(expected, parse("  tag1 tag2 { val1 , val2 }  "));
        assertEquals(expected, parse("{tag1:{tag2:{val1,val2}}}"));
    }

    @Test
    public void parse_complexTag4_success() {
        Value expected = complex(
                pair(number(1), text("val1")),
                pair(text("key2"), text("val2")),
                pair(number(2), complex(text("tag3"), list(text("val3A"), text("val3B")))),
                pair(text("key4"), complex(text("tag4"), list(text("val4A"), text("val4B")))),
                pair(text("key5"), complex(text("tag5A"), complex(text("tag5B"), list(text("val5A"), text("val5B"))))));
        assertEquals(expected, parse("{val1,key2:val2,tag3{val3A,val3B},key4:tag4{val4A,val4B},key5:tag5A tag5B{val5A,val5B}}"));
        assertEquals(expected, parse("  { val1 , key2 : val2 , tag3 { val3A , val3B } , key4 : tag4 { val4A , val4B } , key5 : tag5A tag5B { val5A , val5B } } "));
        assertEquals(expected, parse("{val1,key2:val2,{tag3:{val3A,val3B}},key4:{tag4:{val4A,val4B}},key5:{tag5A:{tag5B:{val5A,val5B}}}}"));
    }

    @Test
    public void parse_complexComplexKey_success() {
        Value expected = complex(
                pair(complex(), text("val1")),
                pair(list(text("key2A"), text("key2B")), text("val2")),
                pair(complex(text("key3"), text("key3val")), text("val3")));
        assertEquals(expected, parse("{@{}:val1,@{key2A,key2B}:val2,@{key3:key3val}:val3}"));
        assertEquals(expected, parse(" { @ { } : val1 , @ { key2A , key2B } : val2 , @ { key3 : key3val } : val3 } "));
    }

    @Test
    public void parse_complexNotClosed_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected EOF."));
        parse(" { key ");
    }

    @Test
    public void parse_complexInvalidColon_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token ':'."));
        parse(" { key : :  value  }  ");
    }

    @Test
    public void parse_complexInvalidColon2_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token ':'."));
        parse(" { key  key2  :  value  }  ");
    }

    @Test
    public void parse_complexInvalidColon3_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token ':'."));
        parse(" {  :  value  }  ");
    }

    @Ignore
    @Test
    public void parse_complexInvalidComma_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token ','."));
        parse(" {  key ,  ,  }  ");
    }

    @Test
    public void parse_topLevelEmpty_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected EOF."));
        parse("  ");
    }

    @Test
    public void parse_topLevelBadToken_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token '~'."));
        parse(" ~ ");
    }

    @Test
    public void parse_topLevelPair_exception() {
        exception.expect(ParserException.class);
        exception.expectMessage(CoreMatchers.equalTo("Unexpected token ':'."));
        parse(" key : value ");
    }
}
