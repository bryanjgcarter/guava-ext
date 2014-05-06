package com.toonetown.guava_ext.collect;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Unit test for ChunkingIterable
 */
public class ChunkingIterableTest {

    private static class TestIterable extends ChunkingIterable<String> {
        private final ImmutableList<String> chunkList;
        private final int numChunks;
        private int numLoads;

        private TestIterable(final ImmutableList<String> chunkList, final int numChunks) {
            this.chunkList = chunkList;
            this.numChunks = numChunks;
        }

        @Override protected List<String> computeNextChunk() {
            if (numLoads < numChunks) {
                numLoads++;
                return Lists.newArrayList(chunkList);
            }
            return null;
        }
    }

    @Test public void testIterate() {
        final TestIterable iterable = new TestIterable(ImmutableList.of("a", "b", "c"), 3);
        assertEquals(iterable.numLoads, 0);
        assertTrue(Iterables.elementsEqual(iterable, Lists.newArrayList("a", "b", "c",
                                                                        "a", "b", "c",
                                                                        "a", "b", "c")));
        assertEquals(iterable.numLoads, 3);
    }

    @Test public void testPartialIterate() {
        final TestIterable iterable = new TestIterable(ImmutableList.of("a"), 3);
        int i = 0;
        for (final String s : iterable) {
            assertEquals(s, "a");
            assertEquals(iterable.numLoads, ++i);
        }
        for (final String s : iterable) {
            assertEquals(s, "a");
            assertEquals(iterable.numLoads, i);
        }
    }

    @Test public void testEmptyChunks() {
        final TestIterable iterable = new TestIterable(ImmutableList.<String>of(), 3);
        assertEquals(iterable.numLoads, 0);
        assertFalse(iterable.iterator().hasNext());
        assertEquals(iterable.numLoads, 3);
    }
}
