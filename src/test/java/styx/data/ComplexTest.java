package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static styx.data.AssertUtils.assertEqualPairs;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.complex;
import static styx.data.Values.empty;
import static styx.data.Values.list;
import static styx.data.Values.number;
import static styx.data.Values.pair;
import static styx.data.Values.text;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;

public class ComplexTest {

    @Test
    public void empty_noArg_success() {
        Complex value = empty();
        assertNotNull(value);
        assertSize(0, value);
        assertSame(value, empty());
    }

    @Test
    public void complex_noArg_success() {
        Complex value = complex();
        assertNotNull(value);
        assertSize(0, value);
        assertSame(value, empty());
    }

    @Test
    public void complex_onePair_success() {
        Complex value = complex(pair(text("A"), text("foo")));
        assertNotNull(value);
        assertSize(1, value);
        assertEquals(text("foo"), value.get(text("A")).get());
    }

    @Test
    public void complex_twoPairs_success() {
        Complex value = complex(pair(text("A"), text("foo")), pair(text("B"), text("bar")));
        assertNotNull(value);
        assertSize(2, value);
        assertEquals(text("foo"), value.get(text("A")).get());
        assertEquals(text("bar"), value.get(text("B")).get());
    }

    @Test
    public void complex_nullArray_success() {
        Complex value = complex((Pair[]) null);
        assertNotNull(value);
        assertSame(value, empty());
    }

    @Test
    public void complex_listOfOne_success() {
        Complex value = complex(Collections.singletonList(pair(text("A"), text("foo"))));
        assertNotNull(value);
        assertSize(1, value);
        assertEquals(text("foo"), value.get(text("A")).get());
    }

    @Test
    public void complex_listOfTwo_success() {
        Complex value = complex(Arrays.asList(pair(text("A"), text("foo")), pair(text("B"), text("bar"))));
        assertNotNull(value);
        assertSize(2, value);
        assertEquals(text("foo"), value.get(text("A")).get());
        assertEquals(text("bar"), value.get(text("B")).get());
    }

    @Test
    public void complex_emptyList_success() {
        Complex value = complex(Collections.emptyList());
        assertSame(value, empty());
    }

    @Test
    public void complex_nullList_success() {
        Complex value = complex((List<Pair>) null);
        assertNotNull(value);
        assertSame(value, empty());
    }

    @Test
    public void complex_mapOfOne_success() {
        Complex value = complex(Collections.singletonMap(text("A"), text("foo")));
        assertNotNull(value);
        assertSize(1, value);
        assertEquals(text("foo"), value.get(text("A")).get());
    }

    @Test
    public void complex_mapOfTwo_success() {
        Map<Value, Value> map = new HashMap<>();
        map.put(text("A"), text("foo"));
        map.put(text("B"), text("bar"));
        Complex value = complex(map);
        assertNotNull(value);
        assertSize(2, value);
        assertEquals(text("foo"), value.get(text("A")).get());
        assertEquals(text("bar"), value.get(text("B")).get());
    }

    @Test
    public void complex_emptyMap_success() {
        Complex value = complex(Collections.emptyMap());
        assertSame(value, empty());
    }

    @Test
    public void complex_nullMap_success() {
        Complex value = complex((Map<Value, Value>) null);
        assertNotNull(value);
        assertSame(value, empty());
    }


    @Test
    public void list_noArg_success() {
        Complex value = list();
        assertNotNull(value);
        assertSize(0, value);
        assertSame(value, empty());
    }

    @Test
    public void list_oneValue_success() {
        Complex value = list(text("foo"));
        assertNotNull(value);
        assertSize(1, value);
        assertEquals(text("foo"), value.get(number(1)).get());
    }

    @Test
    public void list_twoValues_success() {
        Complex value = list(text("foo"), text("bar"));
        assertNotNull(value);
        assertSize(2, value);
        assertEquals(text("foo"), value.get(number(1)).get());
        assertEquals(text("bar"), value.get(number(2)).get());
    }

    @Test
    public void list_nullArray_success() {
        Complex value = list((Value[]) null);
        assertNotNull(value);
        assertSame(value, empty());
    }

    @Test
    public void list_listOfOne_success() {
        Complex value = list(Collections.singletonList(text("foo")));
        assertEquals(value, list(text("foo")));
    }

    @Test
    public void list_listOfTwo_success() {
        Complex value = list(Arrays.asList(text("foo"), text("bar")));
        assertEquals(value, list(text("foo"), text("bar")));
    }

    @Test
    public void list_emptyList_success() {
        Complex value = list(Collections.emptyList());
        assertSame(value, empty());
    }

    @Test
    public void list_nullList_success() {
        Complex value = list((List<Value>) null);
        assertNotNull(value);
        assertSame(value, empty());
    }

    @Ignore
    @Test
    public void list_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> list((Value) null));
        assertException(IllegalArgumentException.class, () -> list(Arrays.asList((Value) null)));
    }

    @Test
    public void iterator_valid_success() {
        Iterator<Pair> it = list(number(1), number(2), number(3)).iterator();
        assertTrue(it.hasNext());
        assertEquals(number(1), it.next().value());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(number(2), it.next().value());
        assertEquals(number(3), it.next().value());
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
        assertException(NoSuchElementException.class, it::next);
    }

    @Test
    public void iterator_empty_success() {
        Iterator<Pair> it = empty().iterator();
        assertFalse(it.hasNext());
        assertException(NoSuchElementException.class, it::next);
    }

    @Test
    public void iterator_forEach_success() {
        Complex value = list(IntStream.rangeClosed(1, 1000).mapToObj(i -> number(i)).collect(Collectors.toList()));
        int expected = 1;
        for(Pair pair : value) {
            assertEquals(number(expected), pair.key());
            assertEquals(number(expected), pair.value());
            expected++;
        }
        assertEquals(1001, expected);
    }

    @Test
    public void get_valid_success() {
        Complex value = complex(pair(text("A"), text("foo")), pair(text("B"), text("bar")));
        assertEquals(text("foo"), value.get(text("A")).get());
        assertEquals(text("bar"), value.get(text("B")).get());
        assertFalse(value.get(text("C")).isPresent());
    }

    @Test
    public void get_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> complex().get(null));
    }

    @Test
    public void put_valid_success() {
        Complex value = list(text("foo"), text("bar"), text("baz"));
        assertEquals(value, value.put(number(4), null)); // no-op
        assertEquals(text("xyz"), value.put(number(3), text("xyz")).get(number(3)).get()); // replace
        assertEquals(empty(), value.put(number(1), null).put(number(2), null).put(number(3), null)); // remove, ascending
        assertEquals(empty(), value.put(number(3), null).put(number(2), null).put(number(1), null)); // remove, descending
    }

    @Test
    public void put_manyAscending_balanced() {
        List<Pair> list = IntStream.rangeClosed(1, 1000).
                mapToObj(i -> pair(number(i), text("Val" + i))).
                collect(Collectors.toList());
        Complex value = complex(list);
        assertEqualPairs(list, value);
        assertEquals(10, height(value));

        for(Pair p : list) {
            value = value.put(p.key(), null);
        }
        assertEquals(empty(), value);
    }

    @Test
    public void put_manyDescending_balanced() {
        List<Pair> list = IntStream.rangeClosed(1, 1000).
                mapToObj(i -> pair(number(-i), text("Val" + i))).
                collect(Collectors.toList());
        Complex value = complex(list);
        assertEqualPairs(list, value);
        assertEquals(10, height(value));

        for(Pair p : list) {
            value = value.put(p.key(), null);
        }
        assertEquals(empty(), value);
    }

    @Test
    public void put_manyRandom_balanced() {
        Random random = new Random(0); // keep it deterministic!
        List<Pair> list = IntStream.rangeClosed(1, 1000).
                mapToObj(i -> pair(number(random.nextLong()), text("Val" + i))).
                collect(Collectors.toList());
        Complex value = complex(list);
        assertEqualPairs(list, value);
        assertEquals(12, height(value));

        for(Pair p : list) {
            value = value.put(p.key(), null);
        }
        assertEquals(empty(), value);
    }

    @Test
    public void put_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> complex().put(null, text("foo")));
    }

    private void assertSize(int expectedSize, Complex value) {
        assertEquals(expectedSize, value.entries().count());
        assertEquals(expectedSize, value.keys().count());
        assertEquals(expectedSize, value.values().count());
        assertEquals(expectedSize, value.allEntries().size());
        assertEquals(expectedSize, value.allKeys().size());
        assertEquals(expectedSize, value.allValues().size());
    }

    private int height(Complex value) {
        try {
            Field field = value.getClass().getDeclaredField("height");
            field.setAccessible(true);
            return (int) field.get(value);
        } catch(Exception e) {
            throw new IllegalStateException("Failed to determine height of tree (instance: " + value.getClass().getName() + ").", e);
        }
    }
}
