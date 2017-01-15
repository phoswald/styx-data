package styx.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class FastStackTest {

    private FastStack<Element> testee = new FastStack<>(Element::new, Element::init);
    private int newCounter = 0;
    private int initCounter = 0;

    @Test
    public void pushReuse() {
        assertEquals(0, testee.size());

        Element e1 = testee.push();

        assertNotNull(e1);
        assertSame(e1, testee.peek());
        assertEquals(1, testee.size());
        assertEquals(1, newCounter);
        assertEquals(1, initCounter);

        Element e2 = testee.push();

        assertNotNull(e2);
        assertSame(e2, testee.peek());
        assertNotSame(e1, e2);
        assertEquals(2, testee.size());
        assertEquals(2, newCounter);
        assertEquals(2, initCounter);

        Element e2b = testee.pop();

        assertSame(e2, e2b);
        assertEquals(1, testee.size());

        Element e3 = testee.push();

        assertNotNull(e3);
        assertSame(e3, testee.peek());
        assertNotSame(e1, e2);
        assertSame(e2, e3);
        assertEquals(2, testee.size());
        assertEquals(2, newCounter);
        assertEquals(3, initCounter);
    }

    class Element {
        Element() {
            newCounter++;
        }
        void init() {
            initCounter++;
        }
    }
}
