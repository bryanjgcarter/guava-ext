package com.toonetown.guava_ext.collect;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

/**
 * Unit test to check UriEscaper
 */
public class UniqueIteratorTest {
    
    private static class CountingIterator extends AbstractIterator<String> {
        private int count;
        private final Iterator<String> i;
        @Override protected String computeNext() {
            if (i.hasNext()) {
                count++;
                return i.next();
            }
            return endOfData();
        }
        public CountingIterator(final Iterator<String> i) { this.i = i; }
    }
    
    @Test
    public void testIterator() {
        final CountingIterator i = new CountingIterator(Lists.newArrayList("a", "b", "c", "b").iterator());
        final Iterator<String> iter = UniqueIterator.wrap(i);
        
        assertEquals(i.count, 0);
        assertEquals(iter.next(), "a");
        assertEquals(i.count, 1);
        assertEquals(iter.next(), "b");
        assertEquals(i.count, 2);
        assertEquals(iter.next(), "c");
        assertEquals(i.count, 3);
        assertFalse(iter.hasNext());
        assertEquals(i.count, 4);
    }

    @Test
    public void testIterator_repeated() {
        final CountingIterator i = new CountingIterator(Lists.newArrayList("a", "a", "b", "c").iterator());
        final Iterator<String> iter = UniqueIterator.wrap(i);
        
        assertEquals(i.count, 0);
        assertEquals(iter.next(), "a");
        assertEquals(i.count, 1);
        assertEquals(iter.next(), "b");
        assertEquals(i.count, 3);
        assertEquals(iter.next(), "c");
        assertEquals(i.count, 4);
        assertFalse(iter.hasNext());
        assertEquals(i.count, 4);
    }

    @Test
    public void testIterator_onlyRepeated() {
        final CountingIterator i = new CountingIterator(Lists.newArrayList("a", "a", "a").iterator());
        final Iterator<String> iter = UniqueIterator.wrap(i);
        
        assertEquals(i.count, 0);
        assertEquals(iter.next(), "a");
        assertEquals(i.count, 1);
        assertFalse(iter.hasNext());
        assertEquals(i.count, 3);
    }

    @Test
    public void testIterator_alreadyUnique() {
        final CountingIterator i = new CountingIterator(Lists.newArrayList("a", "b", "c").iterator());
        final Iterator<String> iter = UniqueIterator.wrap(i);
        
        assertEquals(i.count, 0);
        assertEquals(iter.next(), "a");
        assertEquals(i.count, 1);
        assertEquals(iter.next(), "b");
        assertEquals(i.count, 2);
        assertEquals(iter.next(), "c");
        assertEquals(i.count, 3);
        assertFalse(iter.hasNext());
        assertEquals(i.count, 3);
    }

    @Test
    public void testIterator_empty() {
        final CountingIterator i = new CountingIterator(Lists.<String>newArrayList().iterator());
        final Iterator<String> iter = UniqueIterator.wrap(i);
        
        assertEquals(i.count, 0);
        assertFalse(iter.hasNext());
        assertEquals(i.count, 0);
    }

    @Test
    public void testIterator_fromSet() {
        assertFalse(UniqueIterator.from(Sets.newHashSet()) instanceof UniqueIterator);
    }

    @Test
    public void testIterator_fromList() {
        assertTrue(UniqueIterator.from(Lists.newArrayList()) instanceof UniqueIterator);
    }

}
