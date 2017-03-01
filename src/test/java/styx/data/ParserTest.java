package styx.data;

import static org.junit.Assert.assertEquals;
import static styx.data.AssertUtils.assertException;
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

import org.junit.Test;

public class ParserTest {

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
    public void parse_numberInteger_success() {
        assertEquals(number(0), parse("0"));
        assertEquals(number(0), parse("  0  "));
        assertEquals(number(1234), parse("  1234  "));
        assertEquals(number(-1234), parse("  -1234  "));
    }

    @Test
    public void parse_numberFractional_success() {
        assertEquals(number(0), parse("0.0"));
        assertEquals(number(0.1), parse("  0.1  "));
        assertEquals(number(1234.56789), parse("  001234.5678900  "));
        assertEquals(number(-1234.56789), parse("  -001234.5678900  "));
    }

    @Test
    public void parse_numberExponential_success() {
        assertEquals(number(1000), parse("1E3"));
        assertEquals(number(0.001), parse("  1E-3  "));
        assertEquals(number(1234.56789), parse("  0012.345678900E2  "));
        assertEquals(number(-0.123456789), parse("  -0012.345678900E-2  "));
    }

    @Test
    public void parse_numberBadChars_exception() {
        assertException(ParserException.class, "Invalid numeric value: unexpected token 'X'.", () -> parse(" 1234X "));
        assertException(ParserException.class, "Invalid numeric value: unexpected token '-'.", () -> parse(" -12-34 "));
        assertException(ParserException.class, "Invalid numeric value: unexpected token '.'.", () -> parse(" 12.34.56 "));
        assertException(ParserException.class, "Invalid numeric value: unexpected token 'E'.", () -> parse(" 1E5E7 "));
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
        assertException(ParserException.class, "Invalid textual value: closing '\"' expected.", () -> parse("\""));
    }

    @Test
    public void parse_textQuotedInvalidEsacape_exception() {
        assertException(ParserException.class, "Invalid textual value: invalid escape sequence '\\a'.", () -> parse("\"\\a\""));
        assertException(ParserException.class, "Invalid textual value: closing '\"' expected.", () -> parse("\"\\"));
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
    public void parse_binaryInvalid_exception() {
        assertException(ParserException.class, "Invalid binary value: even number of digits expected.", () -> parse("  0x0  "));
        assertException(ParserException.class, "Invalid binary value: unexpected token 'a'.", () -> parse("  0xDEad  "));
    }

    @Test
    public void parse_referenceValid_success() {
        assertEquals(reference(), parse("</>"));
        assertEquals(reference(text("part1")), parse("</part1>"));
        assertEquals(reference(text("part1"), text("part2")), parse("</part1/part2>"));
        assertEquals(reference(number(1), number(2), number(3)), parse("</1/2/3>"));
        assertEquals(reference(reference(), reference(text("val"))), parse("</</>/</val>>"));
        assertEquals(reference(list(), list(text("val"))), parse("</{}/{val}>"));
    }

    @Test
    public void parse_referenceNotClosed_exception() {
        assertException(ParserException.class, "Invalid reference: '/' expected.", () -> parse("<"));
        assertException(ParserException.class, "Invalid reference: part or '>' expected.", () -> parse("</"));
        assertException(ParserException.class, "Invalid reference: '/' or '>' expected.", () -> parse("</part"));
    }

    @Test
    public void parse_referenceBadToken_exception() {
        assertException(ParserException.class, "Invalid reference: '/' expected.", () -> parse("<,"));
        assertException(ParserException.class, "Invalid reference: part or '>' expected.", () -> parse("</,"));
        assertException(ParserException.class, "Invalid reference: '/' or '>' expected.", () -> parse("</part,"));
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
    public void parse_complexListOfFractions_success() {
        Value expected = complex(pair(number(2), text("val2")), pair(number(2.5), text("val2.5")), pair(number(3), text("val3")));
        assertEquals(expected, parse("{2:val2,2.5:\"val2.5\",val3}"));
        assertEquals(expected, parse("  { 2: val2, 2.5: \"val2.5\", val3 }  "));
        assertEquals(expected, parse("{\n    2: val2\n    2.5: \"val2.5\"\n    val3\n}"));
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
    public void parse_complexComplexKey2_success() {
        Value expected = complex(list(list(text("val1"), text("val2"))), list());
        assertEquals(expected, parse("{@{{val1,val2}}:{}}"));
        assertEquals(expected, parse(" { @ { { val1 , val2 } } : { } } "));
    }

    @Test
    public void parse_complexComplexKey3_success() {
        Value expected = complex(complex(complex(), text("val1")), text("val2"));
        assertEquals(expected, parse("{@{@{}:val1}:val2}"));
        assertEquals(expected, parse("{ @ { @ { } : val1 } : val2 } "));
    }

    @Test
    public void parse_complexNotClosed_exception() {
        assertException(ParserException.class, "Unexpected EOF.", () -> parse(" { key "));
    }

    @Test
    public void parse_complexInvalid_exception() {
        assertException(ParserException.class, "Unexpected token ':'.", () -> parse(" { key : :  value  }  "));
        assertException(ParserException.class, "Unexpected token ':'.", () -> parse(" { key  key2  :  value  }  "));
        assertException(ParserException.class, "Unexpected token ':'.", () -> parse(" {  :  value  }  "));
//      assertException(ParserException.class, "Unexpected token ','.", () -> parse(" {  key ,  ,  }  "));
        assertException(ParserException.class, "Invalid complex key: '{' expected.", () -> parse(" { @ key : val } "));
        assertException(ParserException.class, "Invalid complex key: '{' expected.", () -> parse(" { @ : val } "));
    }

    @Test
    public void parse_topLevelEmpty_exception() {
        assertException(ParserException.class, "Unexpected EOF.", () -> parse("  "));
    }

    @Test
    public void parse_topLevelInvalid_exception() {
        assertException(ParserException.class, "Unexpected token '~'.", () -> parse(" ~ "));
        assertException(ParserException.class, "Unexpected token ':'.", () -> parse(" key : value "));
        assertException(ParserException.class, "Unexpected token ','.", () -> parse(" value , "));
        assertException(ParserException.class, "Unexpected token '}'.", () -> parse(" value } "));
        assertException(ParserException.class, "Unexpected token '@'.", () -> parse(" @ { } "));
    }
}
