package com.toonetown.guava_ext.collect;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;

/**
 * An abstract iterable class that loads its data in chunks.  If you extend this class and implement the
 * computeNextChunk function, then it will behave as a dynamically-loading iterable where computeNextChunk will
 * only get called when needed.
 */
public abstract class ChunkingIterable<T> implements Iterable<T> {
    /** The list of chunks that we have retrieved so far */
    final List<List<T>> chunks = Lists.newArrayList();

    @Override public Iterator<T> iterator() {
        final Iterator<List<T>> chunkedIterator = chunks.iterator();
        return new AbstractIterator<T>() {
            private Iterator<T> currentChunk;
            @Override protected T computeNext() {
                /* Get the next chunk */
                while (currentChunk == null && chunkedIterator.hasNext()) {
                    currentChunk = chunkedIterator.next().iterator();
                }

                /* We are still null - so try and load our next one */
                while (currentChunk == null || !currentChunk.hasNext()) {
                    final List<T> nextChunk = computeNextChunk();
                    if (nextChunk == null) {
                        /* No more chunks - so this is it */
                        return endOfData();
                    }
                    /* Add the new one to our list and keep going */
                    chunks.add(nextChunk);
                    currentChunk = nextChunk.iterator();
                }

                /* If we get here, we have a next value - so return it */
                return currentChunk.next();
            }
        };
    }

    /**
     * Computes and returns the next chunk of results.  Returns null if there are no more results.
     *
     * @return the next chunk of results, or null if there are no more results
     */
    protected abstract List<T> computeNextChunk();

}
