package styx.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static styx.data.AssertUtils.assertException;
import static styx.data.Values.reference;
import static styx.data.Values.root;
import static styx.data.Values.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class ReferenceTest {

    @Test
    public void root_noArg_success() {
        Reference value = root();
        assertNotNull(value);
        assertEquals(0, value.partCount());
        assertFalse(value.parent().isPresent());
        assertSame(value, root());
    }

    @Test
    public void reference_noArg_success() {
        Reference value = reference();
        assertNotNull(value);
        assertEquals(0, value.partCount());
        assertFalse(value.parent().isPresent());
        assertSame(value, root());
    }

    @Test
    public void reference_oneValue_success() {
        Reference value = reference(text("part1"));
        assertNotNull(value);
        assertEquals(1, value.partCount());
        assertEquals(text("part1"), value.partAt(0));
        assertSame(root(), value.parent().get());
    }

    @Test
    public void reference_twoValues_success() {
        Reference value = reference(text("part1"), text("part2"));
        assertNotNull(value);
        assertEquals(2, value.partCount());
        assertEquals(text("part1"), value.partAt(0));
        assertEquals(text("part2"), value.partAt(1));
        assertEquals(root().child(text("part1")), value.parent().get());
    }

    @Test
    public void reference_nullArray_success() {
        Reference value = reference((Value[]) null);
        assertNotNull(value);
        assertEquals(0, value.partCount());
        assertFalse(value.parent().isPresent());
        assertSame(value, root());
    }

    @Test
    public void reference_listOfOne_success() {
        Reference value = reference(Collections.singletonList(text("part1")));
        assertNotNull(value);
        assertEquals(1, value.partCount());
        assertEquals(text("part1"), value.partAt(0));
        assertSame(root(), value.parent().get());
    }

    @Test
    public void reference_listOfTwo_success() {
        Reference value = reference(Arrays.asList(text("part1"), text("part2")));
        assertNotNull(value);
        assertEquals(2, value.partCount());
        assertEquals(text("part1"), value.partAt(0));
        assertEquals(text("part2"), value.partAt(1));
        assertEquals(root().child(text("part1")), value.parent().get());
    }

    @Test
    public void reference_emptyList_success() {
        Reference value = reference(Collections.emptyList());
        assertNotNull(value);
        assertEquals(0, value.partCount());
        assertFalse(value.parent().isPresent());
        assertSame(value, root());
    }

    @Test
    public void reference_nullList_success() {
        Reference value = reference((List<Value>) null);
        assertNotNull(value);
        assertEquals(0, value.partCount());
        assertFalse(value.parent().isPresent());
        assertSame(value, root());
    }

    @Ignore
    @Test
    public void reference_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> reference((Value) null));
        assertException(IllegalArgumentException.class, () -> reference(Arrays.asList((Value) null)));
    }

    @Test
    public void partAt_invalid_exception() {
        assertException(IndexOutOfBoundsException.class, () -> reference().partAt(0));
        assertException(IndexOutOfBoundsException.class, () -> reference(text("part1"), text("part2")).partAt(-1));
        assertException(IndexOutOfBoundsException.class, () -> reference(text("part1"), text("part2")).partAt(2));
    }

    @Test
    public void child_valid_success() {
        assertEquals(reference(text("part1")), reference().child(text("part1")));
        assertEquals(reference(text("part1"), text("part2")), reference(text("part1")).child(text("part2")));
    }

    @Ignore
    @Test
    public void child_invalid_exception() {
        assertException(IllegalArgumentException.class, () -> reference().child((Value) null));
    }
}
